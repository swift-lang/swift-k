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
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public class Block implements StatusListener {
    public static final Logger logger = Logger.getLogger(Block.class);

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
    private SortedSet scpus;
    private List cpus;
    private boolean running, failed, shutdown;
    private BlockQueueProcessor ap;
    private BlockTask task;
    private String id;
    private int doneJobCount;

    private static int sid;

    private static final NumberFormat IDF = new DecimalFormat("000000");

    public Block(String id) {
        this.id = id;
        scpus = new TreeSet();
        cpus = new ArrayList();
    }

    public Block(int workers, TimeInterval walltime, BlockQueueProcessor ap) {
        this(ap.getBQPId() + "-" + IDF.format(sid++));
        this.workers = workers;
        this.walltime = walltime;
        this.ap = ap;
        this.creationtime = Time.now();
        this.deadline = Time.now().add(ap.getSettings().getReserve());
    }

    public void start() {
        logger.info("Starting block: workers=" + workers + ", walltime=" + walltime);
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
        return ap;
    }

    public boolean isDone() {
        if (failed) {
            return true;
        }
        else if (running) {
            Time last = getStartTime();
            synchronized (cpus) {
                Iterator i = cpus.iterator();
                while (i.hasNext()) {
                    Cpu cpu = (Cpu) i.next();
                    if (cpu.getTimeLast().isGreaterThan(last)) {
                        last = cpu.getTimeLast();
                    }
                }
            }
            deadline = Time.min(starttime.add(walltime), last.add(ap.getSettings().getMaxWorkerIdleTime()));
            return Time.now().isGreaterThan(deadline);
        }
        else {
            return false;
        }
    }

    public boolean fits(Job j) {
        if (!running) {
            return j.getMaxWallTime().isGreaterThan(walltime);
        }
        else if (running && j.getMaxWallTime().isGreaterThan(endtime.subtract(Time.now()))) {
            return false;
        }
        else {
            synchronized (cpus) {
                Iterator i = cpus.iterator();
                while (i.hasNext()) {
                    Cpu cpu = (Cpu) i.next();
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
            Cpu last = (Cpu) scpus.last();
            if (last != null) {
                deadline =
                        Time.min(last.getTimeLast().add(ap.getSettings().getReserve()),
                            getEndTime());
            }
        }
    }

    private static final TimeInterval NO_TIME = TimeInterval.fromSeconds(0);

    public double sizeLeft() {
        if (running) {
            return ap.getMetric().size(workers, (int) TimeInterval.max(endtime.subtract(Time.max(Time.now(), starttime)), NO_TIME).getSeconds());
        }
        else {
            return ap.getMetric().size(workers, (int) walltime.getSeconds());
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

    public void shutdown() {
        synchronized (cpus) {
            if (shutdown || failed) {
                return;
            }
            shutdown = true;
            if (running) {
                Iterator i = cpus.iterator();
                while (i.hasNext()) {
                    Cpu cpu = (Cpu) i.next();
                    cpu.shutdown();
                }
            }
            else {
                forceShutdown();
            }
        }
    }

    public void forceShutdown() {
        if (task != null) {
            try {
                getSubmitter().cancel(this);
            }
            catch (Exception e) {
                logger.warn("Failed to shut down block", e);
            }
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
                for (int j = cpus.size(); j < workers; j++) {
                    Cpu cpu = new Cpu(j, this);
                    scpus.add(cpu);
                    cpus.add(cpu);
                }

                Iterator i = cpus.iterator();
                while (i.hasNext()) {
                    Cpu cpu = (Cpu) i.next();
                    cpu.taskFailed(msg, e);
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public String cpuStarted(String sid, ChannelContext channelContext) {
        synchronized (cpus) {
            synchronized (scpus) {
                int id = Integer.parseInt(sid);
                Cpu cpu = new Cpu(id, this);
                scpus.add(cpu);
                cpus.add(cpu);
                cpu.workerStarted(channelContext);
                if (logger.isInfoEnabled()) {
                    logger.info("Started worker " + this.id + ":" + IDF.format(id));
                }
                return IDF.format(id);
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
                            taskFailed(id + "Block task ended prematurely\n"
                                    + prettifyOut(task.getStdOutput())
                                    + prettifyOut(task.getStdError()), null);
                        }
                    }
                    running = false;
                }
                logger.info(id + " stdout: " + prettifyOut(task.getStdOutput()));
                logger.info(id + " stderr: " + prettifyOut(task.getStdError()));
            }
            else if (s.getStatusCode() == Status.ACTIVE) {
                running = true;
                starttime = Time.now();
                endtime = starttime.add(walltime);
                deadline = starttime.add(ap.getSettings().getReserve());
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

    public Collection getCpus() {
        return cpus;
    }

    public boolean isRunning() {
        return running;
    }

    public void increaseDoneJobCount() {
        doneJobCount++;
    }
}