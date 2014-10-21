/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.impl.execution.coaster.SubmitJobCommand;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;

public class Cpu implements Comparable<Cpu>, Callback, ExtendedStatusListener {
    public static final Logger logger = Logger.getLogger(Cpu.class);

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
	private int perfTraceInterval;
	
	public static volatile int totalCompletedJobs, totalFailedJobs, totalJobCount;
	
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
        perfTraceInterval = bqp.getSettings().getPerfTraceInterval();
        timelast = Time.fromSeconds(0);
    }

    public void workerStarted() {
	 if (logger.isInfoEnabled()) {
	        logger.info("worker started: block=" + block.getId() +
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

    private void jobTerminated() {
        assert Thread.holdsLock(this);
        
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

    private PullThread getPullThread(Block block) {
        return block.getAllocationProcessor().getTaskDispatcher();
    }

    private synchronized void pullLater(Cpu cpu) {
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
    public void pull() {
        boolean success = true;
        try {
            if (checkSuspended(block)) {
                return;
            }
            if (logger.isTraceEnabled()) {
                logger.trace(block.getId() + ":" + getId() + " pull");
            }
            if (isShutDown()) {
                return;
            }
            synchronized (this) {
                totalJobCount++;
                if (!started()) {
                    sleep();
                }
                else if (running == null) {
                    lastseq = bqp.getQueueSeq();
                    TimeInterval time = endtime.subtract(Time.now());
                    int cpus = getPullThread().sleepers() + 1;
                    if (logger.isDebugEnabled())
                        logger.debug("requesting work: " +
                                     "block=" + block.getId() +
                                     " id=" + getId() +
                                     " Cpus sleeping: " + cpus);
                    running = bqp.request(time, cpus);
                    if (running != null) {
                        block.jobPulled();
                        if (perfTraceInterval != -1 && (totalJobCount % perfTraceInterval == 0)) {
                            JobSpecification spec = (JobSpecification) running.getTask().getSpecification();
                            spec.setAttribute("tracePerformance", String.valueOf(totalJobCount));
                        }
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
        Task t = running.getTask();
        NotificationManager.getDefault().registerListener(t.getIdentity().getValue(), t, this);
        idleTime += timeDiff();
        timelast = running.getEndTime();
        if (timelast == null) {
            CoasterService.error(20, "Timelast is null", new Throwable());
        }
        block.add(this, timelast);
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
    	return block.isSuspended();
    }

    protected void submit(Job job) {
        Task task = job.getTask();
        if (logger.isDebugEnabled()) {
            JobSpecification spec =
                (JobSpecification) task.getSpecification();
            logger.debug(block.getId() + ":" + getId() + " (quality: " + getQuality() + ")" + 
                " submitting " + task.getIdentity() + ": " +
                spec.getExecutable() + " " + spec.getArguments());
        }
        else if (logger.isInfoEnabled()) {
            JobSpecification spec =
                (JobSpecification) task.getSpecification();
            logger.info(block.getId() + ":" + getId() + " (quality: " + getQuality() + ")" + 
                " submitting " + task.getIdentity());
        }
        task.setStatus(Status.SUBMITTING);
        try {
            CoasterChannel channel = node.getChannel();
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
        if (raiseShutdown()) {
            return;
        }
        synchronized(this) {
    		Block block = node.getBlock();
            done.clear();
            if (running != null) {
                logger.info(block.getId() + "-" + id + ": Job still running while shutting down");
                running.fail("Shutting down worker", null);
            }
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

    public synchronized Time getTimeLast() {
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
    
    private Object shutdownLock = new Object();
    
    private boolean raiseShutdown() {
        synchronized(shutdownLock) {
            if (shutdown) {
                return true;
            }
            shutdown = true;
            return false;
        }
    }
    
    private boolean isShutDown() {
        synchronized(shutdownLock) {
            return shutdown;
        }
    }

    public void taskFailed(String msg, Exception e) {
        if (raiseShutdown()) {
            return;
        }
        synchronized(this) {
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
                running.fail("Block task failed: " + msg, e);
            }
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

     public synchronized void statusChanged(Status s, String out, String err) {
         if (logger.isDebugEnabled()) {
             logger.debug("WORKER_JOB_STATUS " + block.getId() + ":" + getId() + 
                 " (" + System.identityHashCode(this) + ") " + s);
         }
         if (s.isTerminal()) {
             running.setEndTime(Time.now());
             if (s.getStatusCode() == Status.FAILED) {
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
