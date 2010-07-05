//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 21, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TimerTask;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public class Block implements StatusListener, Comparable<Block> {
    public static final Logger logger = Logger.getLogger(Block.class);

    public static final long SHUTDOWN_WATCHDOG_DELAY = 2 * 60 * 1000;
    
    public static final long SUSPEND_SHUTDOWN_DELAY = 30 * 1000;

    private static BlockTaskSubmitter submitter;

    private synchronized static BlockTaskSubmitter getSubmitter() {
        if (submitter == null) {
            submitter = new BlockTaskSubmitter();
            submitter.start();
        }
        return submitter;
    }

    private int workers, qt;
    private TimeInterval walltime;
    private Time endtime, starttime, deadline, creationtime;
    private SortedSet<Cpu> scpus;
    private List<Cpu> cpus;
    private List<Node> nodes;
    private boolean running, failed, shutdown, suspended;
    private BlockQueueProcessor bqp;
    private BlockTask task;
    private String id;
    private int doneJobCount;
    private long lastUsed;

    private static int sid;

    private static final NumberFormat IDF = new DecimalFormat("000000");

    public Block(String id) {
        this.id = id;
        scpus = new TreeSet<Cpu>();
        cpus = new ArrayList<Cpu>();
        nodes = new ArrayList<Node>();
    }
    
    public Block(int workers, TimeInterval walltime, BlockQueueProcessor ap) {
        this(ap.getBQPId() + "-" + IDF.format(sid++), workers, walltime, ap);
    }

    public Block(String id, int workers, TimeInterval walltime, BlockQueueProcessor ap) {
        this(id);
        this.workers = workers;
        this.walltime = walltime;
        this.bqp = ap;
        this.creationtime = Time.now();
        this.deadline = Time.now().add(ap.getSettings().getReserve());
    }

    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting block: workers=" + workers + ", walltime=" + walltime);
        }
        bqp.getRLogger().log(
            "BLOCK_REQUESTED id=" + getId() + ", w=" + getWorkerCount() + ", h="
                    + getWalltime().getSeconds());
        task = new BlockTask(this);
        task.addStatusListener(this);
        try {
            task.initialize();
            getSubmitter().submit(this);
        }
        catch (Exception e) {
            taskFailed(null, e);
        }
    }

    public BlockQueueProcessor getAllocationProcessor() {
        return bqp;
    }

    public boolean isDone() {
        if (failed) {
            return true;
        }
        else if (running) {
            Time last = getStartTime();
            synchronized (cpus) {
                for (Cpu cpu: cpus) {
                    if (cpu.getTimeLast().isGreaterThan(last)) {
                        last = cpu.getTimeLast();
                    }
                }
                if (cpus.isEmpty()) {
                    // prevent block from being done when startup of workers is
                    // really really slow,
                    // like as on the BGP where it takes a couple of minutes to
                    // initialize a partition
                    last = Time.now();
                }
            }
            deadline =
                    Time.min(starttime.add(walltime),
                        last.add(bqp.getSettings().getMaxWorkerIdleTime()));
            return Time.now().isGreaterThan(deadline);
        }
        else {
            return false;
        }
    }

    public boolean fits(Job j) {
        if (suspended) {
            return false;
        }
        if (!running) {
            return j.getMaxWallTime().isGreaterThan(walltime);
        }
        else if (running && j.getMaxWallTime().isGreaterThan(endtime.subtract(Time.now()))) {
            return false;
        }
        else {
            synchronized (cpus) {
                for (Cpu cpu : cpus) {
                    Job running = cpu.getRunning();
                    if (running == null) {
                        return true;
                    }
                    else if (j.getMaxWallTime().isLessThan(endtime.subtract(running.getEndTime()))) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public void remove(Cpu cpu) {
        synchronized (scpus) {
            if (!scpus.remove(cpu)) {
                CoasterService.error(16, "CPU was not in the block", new Throwable());
            }
            if (scpus.contains(cpu)) {
                CoasterService.error(17, "CPU not removed", new Throwable());
            }
        }
    }

    public void add(Cpu cpu) {
        synchronized (scpus) {
            if (!scpus.add(cpu)) {
                CoasterService.error(15, "CPU is already in the block", new Throwable());
            }
            Cpu last = scpus.last();
            if (last != null) {
                deadline =
                        Time.min(last.getTimeLast().add(bqp.getSettings().getReserve()),
                            getEndTime());
            }
        }
    }
    
    public void cpuIsClear(Cpu cpu) {
        synchronized (scpus) {
            if (scpus.isEmpty()) {
                shutdown(false);
            }
        }
    }

    private static final TimeInterval NO_TIME = TimeInterval.fromSeconds(0);

    public double sizeLeft() {
        if (failed) {
            return 0;
        }
        else if (running) {
            return bqp.getMetric().size(
                workers,
                (int) TimeInterval.max(endtime.subtract(Time.max(Time.now(), starttime)), NO_TIME).getSeconds());
        }
        else {
            return bqp.getMetric().size(workers, (int) walltime.getSeconds());
        }
    }

    public Time getEndTime() {
        if (starttime == null) {
            return Time.now().add(walltime);
        }
        else {
            return starttime.add(walltime);
        }
    }

    public void setEndTime(Time t) {
        this.endtime = t;
    }

    public int getWorkerCount() {
        return workers;
    }

    public void setWorkerCount(int v) {
        this.workers = v;
    }

    public TimeInterval getWalltime() {
        return walltime;
    }

    public void setWalltime(TimeInterval t) {
        this.walltime = t;
    }

    public void shutdown(boolean now) {
        synchronized (cpus) {
            if (shutdown) {
                return;
            }
            logger.info("Shutting down block " + this);
            bqp.getRLogger().log("BLOCK_SHUTDOWN id=" + getId());
            shutdown = true;
            long busyTotal = 0;
            long idleTotal = 0;
            int count = 0;
            if (running) {
                for (Cpu cpu : cpus) {
                    idleTotal = cpu.idleTime;
                    busyTotal = cpu.busyTime;
                    if (!failed) {
                        cpu.shutdown();
                    }
                    count++;
                }
                if (!failed) {
                    addForcedShutdownWatchdog(SHUTDOWN_WATCHDOG_DELAY);
                }
                if (idleTotal > 0) {
                    double u = (busyTotal * 10000) / (busyTotal + idleTotal);
                    u /= 100;
                    logger.info("Average utilization: " + u + "%");
                    bqp.getRLogger().log("BLOCK_UTILIZATION id=" + getId() + ", u=" + u);
                }
                if ((count < workers || now) && !failed) {
                    addForcedShutdownWatchdog(100);
                }
            }
            else {
                logger.info("Block " + this + " not running. Cancelling job.");
                forceShutdown();
            }
            cpus.clear();
        }
    }

    private void addForcedShutdownWatchdog(long delay) {
        CoasterService.addWatchdog(new TimerTask() {
            public void run() {
                if (running) {
                    forceShutdown();
                }
            }
        }, delay);
    }

    public void forceShutdown() {
        if (task != null) {
            try {
                getSubmitter().cancel(this);
            }
            catch (Exception e) {
                logger.warn("Failed to shut down block", e);
            }
            bqp.blockTaskFinished(this);
        }
    }

    public BlockTask getTask() {
        return task;
    }

    public void taskFailed(String msg, Exception e) {
        logger.warn("Worker task failed: " + msg, e);
        synchronized (cpus) {
            synchronized (scpus) {
                failed = true;
                running = false;
                for (int j = cpus.size(); j < workers; j++) {
                    Cpu cpu = new Cpu(j, new Node(j, this, null));
                    scpus.add(cpu);
                    cpus.add(cpu);
                }

                for (Cpu cpu : cpus) {
                    cpu.taskFailed(msg, e);
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public String workerStarted(String sid, ChannelContext channelContext) {
        synchronized (cpus) {
            synchronized (scpus) {
                int id = Integer.parseInt(sid);
                Node n = new Node(id, this, channelContext);
                nodes.add(n);
                for (int i = 0; i < bqp.getSettings().getWorkersPerNode(); i++) {
                    //this id scheme works out because the sid is based on the
                    //number of cpus already added (i.e. cpus.size()).
                    Cpu cpu = new Cpu(id + i, n);
                    scpus.add(cpu);
                    cpus.add(cpu);
                    n.add(cpu);
                    cpu.workerStarted();
                    logger.info("Started CPU " + cpu);
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Started worker " + this.id + ":" + IDF.format(id));
                }
                return sid;
            }
        }
    }

    public String nextId() {
        synchronized (cpus) {
            int id = cpus.size();
            return IDF.format(id);
        }
    }

    public String toString() {
        return "Block " + id + " (" + workers + "x" + walltime + ")";
    }

    public void statusChanged(StatusEvent event) {
        if (logger.isInfoEnabled()) {
            logger.info("Block task status changed: " + event.getStatus());
        }
        try {
            Status s = event.getStatus();
            if (s.isTerminal()) {
                synchronized (cpus) {
                    if (!shutdown) {
                        if (s.getStatusCode() == Status.FAILED) {
                            logger.info("Failed task spec: "
                                    + ((Task) event.getSource()).getSpecification());
                            taskFailed(prettifyOut(task.getStdOutput())
                                    + prettifyOut(task.getStdError()), s.getException());
                        }
                        else {
                            taskFailed(id + " Block task ended prematurely\n"
                                    + prettifyOut(task.getStdOutput())
                                    + prettifyOut(task.getStdError()), null);
                        }
                    }
                    bqp.blockTaskFinished(this);
                    running = false;
                }
                logger.info(id + " stdout: " + prettifyOut(task.getStdOutput()));
                logger.info(id + " stderr: " + prettifyOut(task.getStdError()));
            }
            else if (s.getStatusCode() == Status.ACTIVE) {
                running = true;
                starttime = Time.now();
                endtime = starttime.add(walltime);
                deadline = starttime.add(bqp.getSettings().getReserve());
                bqp.getRLogger().log("BLOCK_ACTIVE id=" + getId());
                bqp.getSettings().getHook().blockActive(event);
            }
        }
        catch (Exception e) {
            CoasterService.error(14, "Failed to process block task status change", e);
        }
    }

    private String prettifyOut(String out) {
        if (out == null) {
            return "";
        }
        else {
            return out + "\n";
        }
    }

    public Time getStartTime() {
        return starttime;
    }

    public void setStartTime(Time t) {
        this.starttime = t;
    }

    public Time getDeadline() {
        return deadline;
    }

    public void setDeadline(Time t) {
        this.deadline = t;
    }

    public Time getCreationTime() {
        return creationtime;
    }

    public void setCreationTime(Time t) {
        this.creationtime = t;
    }

    public Collection<Cpu> getCpus() {
        return cpus;
    }

    public boolean isRunning() {
        return running;
    }
    
    public void setRunning(boolean running) {
        this.running = running;
    }

    public void increaseDoneJobCount() {
        doneJobCount++;
    }

    public void suspend() {
        suspended = true;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void jobPulled() {
        lastUsed = System.currentTimeMillis();
    }
    
    public long getLastUsed() {
        return lastUsed;
    }

    public int compareTo(Block o) {
        return id.compareTo(o.id);
    }
}