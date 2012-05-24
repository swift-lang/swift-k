//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 21, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.execution.coaster.SubmitJobCommand;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.channels.ChannelListener;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;
import org.globus.cog.karajan.workflow.service.commands.HeartBeatCommand;

public class Cpu implements Comparable<Cpu>, Callback, StatusListener {
    public static final Logger logger = Logger.getLogger(Cpu.class);

    private static PullThread pullThread;

    private int id;
    private final List<Job> done;
    private Job running;
    private Block block;
    BlockQueueProcessor bqp;

    private Node node;
    private Time starttime, endtime, timelast, donetime;
    private int lastseq;
    protected long busyTime, idleTime, lastTime;
	private boolean shutdown;
	private int failedJobs, completedJobs;
	
	public static volatile int totalCompletedJobs, totalFailedJobs;

    public Cpu() {
        this.done = new ArrayList<Job>();
        this.timelast = Time.fromMilliseconds(0);
    }

    public Cpu(int id, Node node) {
        this();
        this.id = id;
        this.node = node;
        this.block = node.getBlock();
        this.bqp = block.getAllocationProcessor();
        timelast = Time.fromSeconds(0);
    }

    public void workerStarted() {
	 if (logger.isDebugEnabled()) {
	        logger.debug("worker started: block=" + block.getId() +
	                     " host=" + getNode().getHostname() +
	                     " id=" + id);
	 }
        node.getBlock().remove(this);
        starttime = Time.now();
        endtime = starttime.add(node.getBlock().getWalltime());
        timelast = starttime;
        timeDiff();
        pullLater();
    }
    
    public double getQuality() {
        int failed = failedJobs;
        if (failed < 0) {
            failed = 0;
        }
        return (double) (completedJobs + 1) / (failed + 1);
    }

    private long timeDiff() {
        long now = System.currentTimeMillis();
        long dif = now - lastTime;
        lastTime = now;
        return dif;
    }

    public synchronized void jobTerminated() {
        if (logger.isInfoEnabled()) {
            logger.info(block.getId() + ":" + getId() + " jobTerminated");
        }
        block.increaseDoneJobCount();
        block.remove(this);
        block.jobCompleted();
        donetime = Time.now();
        timelast = donetime;
        busyTime += timeDiff();
        // done.add(running);
        bqp.jobTerminated(running);
        running = null;
        if (!checkSuspended(block)) {
            pullLater();
        }
    }

    private void pullLater() {
        pullLater(this);
    }

    private void sleep() {
        if (logger.isDebugEnabled()) {
            logger.debug(block.getId() + ":" + getId() + " sleeping");
        }
        sleep(this);
    }

    private PullThread getPullThread() {
        return getPullThread(block);
    }

    static PullThread getPullThread(Block block) {
        if (pullThread == null) {
            pullThread = new PullThread(block.getAllocationProcessor());
            pullThread.start();
        }
        return pullThread;
    }

    private static synchronized void pullLater(Cpu cpu) {
        Block block = cpu.node.getBlock();
        if (logger.isDebugEnabled()) {
            logger.debug("ready for work: block=" + block.getId() +
                         " id=" + cpu.getId());
        }
        getPullThread(block).enqueue(cpu);
    }

    private synchronized void sleep(Cpu cpu) {
        getPullThread(cpu.node.getBlock()).sleep(cpu);
    }

    private boolean started() {
        return starttime != null;
    }

    /**
       The Cpu requests work from the BlockQueueProcessor
       The Cpu is awake when calling this (not in PullThread.sleeping)
     */
    public synchronized void pull() {
        boolean success = true;
        try {
            if (checkSuspended(block)) {
                return;
            }
            if (logger.isTraceEnabled()) {
                logger.trace(block.getId() + ":" + getId() + " pull");
            }
            if (shutdown) {
                return;
            }
            if (!started()) {
                sleep();
            }
            else if (running == null) {
                lastseq = bqp.getQueueSeq();
                TimeInterval time = endtime.subtract(Time.now());
                int cpus = pullThread.sleepers() + 1;
                if (logger.isDebugEnabled())
                    logger.debug("requesting work: " +
                                 "block=" + block.getId() +
                                 " id=" + getId() +
                                 " Cpus sleeping: " + cpus);
                running = bqp.request(time, cpus);
                if (running != null) {
                    block.jobPulled();
                    success = launch(running);
                }
                else {
                    sleep();
                }
            }
            else {
                CoasterService.error(40, "pull called while another job was running",
                    new Throwable());
            }
        }
        catch (Exception e) {
            taskFailed("Failed pull", e);
            e.printStackTrace();
            CoasterService.error(21, "Failed pull", e);
        }
        if (! success)
            CoasterService.error(22, "Launch failed");
    }

    boolean launch(Job job) {
        boolean result = false;
        running = job;
        running.start();
        if (job.mpiProcesses == 1)
            result = launchSequential();
        else
            result = launchMPICH(job);
        return result;
    }

    boolean launchSequential() {
        running.getTask().addStatusListener(this);
        idleTime += timeDiff();
        timelast = running.getEndTime();
        if (timelast == null) {
            CoasterService.error(20, "Timelast is null", new Throwable());
        }
        block.add(this);
        submit(running);
        return true;
    }

    /**
       Transform an MPI Job into an MPIEXEC job and a set of
       Hydra proxy jobs.  Then launch these individually.
       Requires non-standard mpiexec with Hydra as modified by
       Justin Wozniak

       The Cpu Count is the number of Cpus required

       This Cpu is awake but n-1 others must be obtained via
       PullThread.getSleepers()
     */
    boolean launchMPICH(Job job) {
    	int cpuCount = job.mpiProcesses/job.mpiPPN;
        List<Cpu> cpus = getPullThread().getSleepers(cpuCount-1);
        cpus.add(this);
        Mpiexec mpiexec = new Mpiexec(cpus, job);
        boolean result = mpiexec.launch();
        return result;
    }

    private boolean checkSuspended(Block block) {
        if (block.isSuspended()) {
            block.shutdownIfEmpty(this);
            return true;
        }
        else {
            return false;
        }
    }

    protected void submit(Job job) {
        Task task = job.getTask();
        if (logger.isInfoEnabled()) {
            JobSpecification spec =
                (JobSpecification) task.getSpecification();
            logger.info(block.getId() + ":" + getId() + " (quality: " + getQuality() + ")" + 
                " submitting " + task.getIdentity() + ": " +
                spec.getExecutable() + " " + spec.getArguments());
        }
        task.setStatus(Status.SUBMITTING);
        try {
            KarajanChannel channel = node.getChannel();
            SubmitJobCommand cmd = new SubmitJobCommand(task);
            cmd.setCompression(false);
            cmd.setSimple(true);
            cmd.executeAsync(channel, this);
        }
        catch (Exception e) {
            logger.info(block.getId() + ":" + getId() + " submission failed " + task.getIdentity());
            taskFailed(null, e);
        }
    }

    public void shutdown() {
        synchronized(this) {
            if (shutdown) {
                return;
            }
            shutdown = true;
        }
		Block block = node.getBlock();
        done.clear();
        if (running != null) {
            logger.info(block.getId() + "-" + id + ": Job still running while shutting down");
            running.fail("Shutting down worker", null);
        }
        node.shutdown();
    }

    public int compareTo(Cpu o) {
        TimeInterval diff = timelast.subtract(o.timelast);
        if (diff.getMilliseconds() == 0) {
            return id - o.id;
        }
        else {
            return (int) diff.getMilliseconds();
        }
    }

    public synchronized Job getRunning() {
        return running;
    }

    public Time getTimeLast() {
        if (running != null) {
            if (timelast.isGreaterThan(Time.now())) {
                return timelast;
            }
            else {
                return Time.now();
            }
        }
        else {
            return timelast;
        }
    }

    @Override
    public String toString() {
        return id + ":" + timelast;
    }

    public List<Job> getDoneJobs() {
        return done;
    }

    public synchronized void taskFailed(String msg, Exception e) {
		shutdown = true;
        if (running == null) {
            if (starttime == null) {
                starttime = Time.now();
            }
            if (endtime == null) {
                endtime = starttime.add(block.getWalltime());
            }
            TimeInterval time = endtime.subtract(Time.now());
            int cpus = 1 + getPullThread(node.getBlock()).sleepers();
            running = bqp.request(time, cpus);
            // no listener is added to this task, so make sure
            // it won't linger in the BQP running set
            bqp.jobTerminated(running);
        }
        if (running != null) {
            running.fail("Task failed: " + msg, e);
        }
    }

    public void errorReceived(Command cmd, String msg, Exception t) {
        taskFailed(msg, t);
    }

    public void replyReceived(Command cmd) {
        SubmitJobCommand sjc = (SubmitJobCommand) cmd;
        Task task = sjc.getTask();
        task.setStatus(Status.SUBMITTED);
    }

     public synchronized void statusChanged(StatusEvent event) {
         if (logger.isDebugEnabled()) {
             logger.debug(event);
         }
         if (event.getStatus().isTerminal()) {
             running.getTask().removeStatusListener(this);
             running.setEndTime(Time.now());
             if (event.getStatus().getStatusCode() == Status.FAILED) {
                 failedJobs++;
                 totalFailedJobs++;
             }
             else {
                 completedJobs++;
                 totalCompletedJobs++;
             }
             jobTerminated();
         }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLastSeq() {
        return lastseq;
    }

    public Block getBlock() {
        return block;
    }

    public void setRunning(Job r) {
        if (logger.isDebugEnabled()) {
            logger.debug("setRunning: " + r);
        }
        this.running = r;
    }

    public void addDoneJob(Job d) {
        done.add(d);
    }

    Node getNode() {
        return node;
    }

    String getFullId() {
        return block.getId() + ":" +
               getNode().getHostname() + ":" +
               getId();
    }
}
