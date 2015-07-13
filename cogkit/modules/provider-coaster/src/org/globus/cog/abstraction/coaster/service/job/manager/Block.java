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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimerTask;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.channels.CoasterChannel;

public class Block implements StatusListener, Comparable<Block> {
    public static final Logger logger = Logger.getLogger(Block.class);

    /** milliseconds */
    public static final long SHUTDOWN_WATCHDOG_DELAY = 2 * 60 * 1000;

    /** milliseconds */
    public static final long SUSPEND_SHUTDOWN_DELAY = 30 * 1000;

    private static BlockTaskSubmitter submitter;

    private synchronized static BlockTaskSubmitter getSubmitter() {
        if (submitter == null) {
            submitter = new BlockTaskSubmitter();
            submitter.start();
        }
        return submitter;
    }

    private int workers, activeWorkers;
    private TimeInterval walltime, maxIdleTime;
    private Time endtime, starttime, deadline, creationtime, terminationtime;
    private final SortedMap<Cpu, Time> scpus;
    private final List<Cpu> cpus;
    private final List<Node> nodes;
    private boolean running = false, failed, shutdown, suspended;
    private AbstractBlockWorkerManager bqp;
    private BlockTask task;
    private final String id;
    private int doneJobCount;
    private long lastUsed;

    private static int sid;
    
    private synchronized static int nextSID() {
    	return sid++;
    }

    public static final NumberFormat IDF = new DecimalFormat("000000");
    
    public static int totalRequestedWorkers, totalActiveWorkers, totalFailedWorkers, totalCompletedWorkers, totalCompletedJobs;

    public Block(String id) {
        this.id = id;
        scpus = new TreeMap<Cpu, Time>();
        cpus = new ArrayList<Cpu>();
        nodes = new ArrayList<Node>();
    }

    public Block(int workers, TimeInterval walltime, TimeInterval maxIdleTime, AbstractBlockWorkerManager ap) {
        this(ap.getBQPId() + "-" + IDF.format(nextSID()), workers, walltime, maxIdleTime, ap);
    }

    public Block(String id, int workers, TimeInterval walltime, TimeInterval maxIdleTime, AbstractBlockWorkerManager ap) {
        this(id);
        this.workers = workers;
        this.walltime = walltime;
        this.maxIdleTime = maxIdleTime;
        this.bqp = ap;
        this.creationtime = Time.now();
        setDeadline(Time.now().add(ap.getSettings().getReserve()));
        totalRequestedWorkers += workers;
    }

    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting block: workers=" + workers + ", walltime=" + walltime);
        }
        bqp.getRLogger().log(
            "BLOCK_REQUESTED id=" + getId() + ", cores=" + getWorkerCount() + ", coresPerWorker=" + 
                    bqp.getSettings().getCoresPerNode() + ", walltime=" + getWalltime().getSeconds());
        task = new BlockTask(this, getSubmitter());
        synchronized(cpus) {
            activeWorkers = 0;
        }
        task.addStatusListener(this);
        try {
            task.initialize();
            task.submit();
        }
        catch (Exception e) {
            taskFailed(null, e);
        }
    }

    public AbstractBlockWorkerManager getAllocationProcessor() {
        return bqp;
    }

    public boolean isDone() {
        if (failed) {
            return true;
        }
        else if (running) {
            Time last = getStartTime();
            List<Cpu> active = new ArrayList<Cpu>();
            synchronized (cpus) {
            	active.addAll(cpus);
            }
            for (Cpu cpu: active) {
                if (cpu.getTimeLast().isGreaterThan(last)) {
                    last = cpu.getTimeLast();
                }
            }
            if (active.isEmpty()) {
                // prevent block from being done when startup of workers is
                // really really slow,
                // like as on the BGP where it takes a couple of minutes to
                // initialize a partition
                last = Time.now();
            }
            Time deadline = Time.min(starttime.add(walltime),
                        last.add(maxIdleTime));
            setDeadline(deadline);
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
            // if the block isn't running then the job might fit if its walltime
            // is smaller than the block's walltime
            return j.getMaxWallTime().isLessThan(walltime);
        }
        else if (running && j.getMaxWallTime().isGreaterThan(endtime.subtract(Time.now()))) {
            // if the block is running and the job's walltime is greater than
            // the blocks remaining walltime, then the job doesn't fit
            return false;
        }
        else {
            // if the simple tests before fail try to see if there is a
            // cpu that can specifically fit this job.
            synchronized (cpus) {
                if (scpus.size() < cpus.size()) {
                    // there are some cores not running any jobs
                    return true;
                }
                if (scpus.size() == 0) {
                    // started, but workers not connected yet
                    return true;
                }
                Cpu cpu = scpus.firstKey();
                Time jobEndTime = scpus.get(cpu);
                if (j.getMaxWallTime().isLessThan(endtime.subtract(jobEndTime))) {
                    return true;
                }
            }
            return false;
        }
    }

    public void remove(Cpu cpu) {
        synchronized (cpus) {
            if (scpus.remove(cpu) == null) {
                if (!shutdown) {
                    CoasterService.error(16, "CPU was not in the block", new Throwable());
                }
            }
            if (scpus.containsKey(cpu)) {
                CoasterService.error(17, "CPU not removed", new Throwable());
            }
        }
    }

    public void add(Cpu cpu, Time estJobCompletionTime) {
    	Cpu last = null;
        synchronized (cpus) {
            if (scpus.put(cpu, estJobCompletionTime) != null) {
                CoasterService.error(15, "CPU is already in the block", new Throwable());
            }
            last = scpus.lastKey();
        }
        if (last == cpu) {
            setDeadline(Time.min(estJobCompletionTime.add(bqp.getSettings().getReserve()),
                getEndTime()));
        }
    }

    public boolean shutdownIfEmpty(Cpu cpu) {
        synchronized (cpus) {
            if (scpus.isEmpty()) {
                if (logger.isInfoEnabled() && !shutdown) {
                    logger.info(this + ": all cpus are clear");
                }
                shutdown(false);
                return true;
            }
            else {
            	return false;
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
    
    public int getActiveWorkerCount() {
        synchronized (cpus) {
            return activeWorkers;
        }
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
        List<Cpu> cpusToShutDown;
        synchronized (cpus) {
            if (shutdown) {
                return;
            }
            shutdown = true;
            cpusToShutDown = new ArrayList<Cpu>(cpus);
            cpus.clear();
        }
        logger.info("Shutting down block " + this);
        bqp.getRLogger().log("BLOCK_SHUTDOWN id=" + getId());
        
        long busyTotal = 0;
        long idleTotal = 0;
        int count = 0;
        if (running) {
            for (Cpu cpu : cpusToShutDown) {
                idleTotal = cpu.idleTime;
                busyTotal = cpu.busyTime;
                if (!failed) {
                    cpu.shutdown();
                }
                count++;
            }
			if (!failed) {
				if (count < workers || now) {
				    if (logger.isInfoEnabled()) {
				        logger.info("Adding short shutdown watchdog: count = " + 
				            count + ", workers = " + workers + ", now = " + now);
				    }
                    addForcedShutdownWatchdog(1000);
	            }
				else {
				    if (logger.isInfoEnabled()) {
				        logger.info("Adding normal shutdown watchdog");
				    }
   					addForcedShutdownWatchdog(SHUTDOWN_WATCHDOG_DELAY);
				}
			}

            if (idleTotal > 0) {
                double u = (busyTotal * 10000) / (busyTotal + idleTotal);
                u /= 100;
                logger.info("Average utilization: " + u + "%");
                bqp.getRLogger().log("BLOCK_UTILIZATION id=" + getId() + ", u=" + u);
            }
        }
        else {
            logger.info("Block " + this + " not running. Cancelling job.");
            forceShutdown();
        }
    }

    private void addForcedShutdownWatchdog(long delay) {
        CoasterService.addWatchdog(new TimerTask() {
            @Override
            public void run() {
                if (running) {
                    logger.info("Watchdog: forceShutdown: " + Block.this);
                    forceShutdown();
                }
            }
        }, delay);
    }

    public void forceShutdown() {
        synchronized(cpus) {
            if (task != null) {
                try {
                    getSubmitter().cancel(this);
                }
                catch (Exception e) {
                    logger.debug("Failed to shut down block: " + this, e);
                }
                bqp.blockTaskFinished(this);
            }
        }
    }

    public BlockTask getTask() {
        return task;
    }

    public void taskFailed(String msg, Exception e) {
        if (logger.isInfoEnabled()) {
            logger.info("Worker task failed: " + msg, e);
        }
        // use auxiliary list to avoid deadlocks when
        // cpus get notified separately by the dead channel
        List<Cpu> cpusToNotify = new ArrayList<Cpu>();
        synchronized (cpus) {
            totalRequestedWorkers -= workers;
            totalActiveWorkers -= this.activeWorkers; // only count the ones actually started
            failed = true;
            running = false;
            for (int j = cpus.size(); j < (workers - this.activeWorkers); j++) {
                Cpu cpu = new Cpu(j, new Node(j, this, null));
                scpus.put(cpu, null);
                cpus.add(cpu);
            }
            this.activeWorkers = 0;
            cpusToNotify.addAll(cpus);
        }
        for (Cpu cpu : cpusToNotify) {
            cpu.taskFailed(msg, e);
        }
    }

    public String getId() {
        return id;
    }

    public String workerStarted(String workerID, String workerHostname,
            CoasterChannel channel, Map<String, String> options) {
    	running = true;
        int concurrency = 1;
        if (options.containsKey("concurrency")) {
            concurrency = Integer.parseInt(options.get("concurrency"));
        }
        else {
            concurrency = bqp.getSettings().getJobsPerNode();
        }
        synchronized (cpus) {
            int wid = Integer.parseInt(workerID);
            Node n = new Node(wid, this, workerHostname, channel, concurrency);
            nodes.add(n);
            this.activeWorkers += concurrency;
            totalActiveWorkers += concurrency;
            bqp.getRLogger().log("WORKER_ACTIVE blockid=" + getId() + " id=" + workerID + " node=" + workerHostname + " cores=" + concurrency);
            for (int i = 0; i < concurrency; i++) {
                //this id scheme works out because the sid is based on the
                //number of cpus already added (i.e. cpus.size()).
                Cpu cpu = new Cpu(wid + i, n);
                scpus.put(cpu, cpu.getTimeLast());
                cpus.add(cpu);
                n.add(cpu);
                cpu.workerStarted();
                if (logger.isInfoEnabled()) {
                    logger.info("Started CPU " + cpu);
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Started worker " + this.id + ":" + IDF.format(wid));
            }
            return workerID;
        }
    }
    
    private int seq;

    public String nextId() {
        synchronized (this) {
            int n = seq;
            seq += bqp.getSettings().getJobsPerNode();
            return IDF.format(n);
        }
    }

    @Override
    public String toString() {
        return "Block " + id + " (" + workers + "x" + walltime + ")";
    }
    
    public List<Node> getNodes() {
        return nodes;
    }
    
    public Node findNode(String nodeID) {
        Integer id = Integer.parseInt(nodeID);
        synchronized (cpus) {
            for (Node n : nodes) {
                if (n.getId() == id) {
                    return n;
                }
            }
        }
        return null;
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
                        bqp.getRLogger().log("BLOCK_FAILED id=" + getId());
                        totalFailedWorkers += workers;
                    }
                    else {
                        totalCompletedWorkers += workers;
                        bqp.getRLogger().log("BLOCK_DONE id=" + getId());
                    }
                    bqp.blockTaskFinished(this);
                    totalActiveWorkers -= this.activeWorkers;
                    running = false;
                    task = null;
                }
                if (terminationtime == null) {
                    terminationtime = Time.now();
                }
            }
            else if (s.getStatusCode() == Status.ACTIVE) {
                starttime = Time.now();
                endtime = starttime.add(walltime);
                setDeadline(starttime.add(bqp.getSettings().getReserve()));
                running = true;
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

    public Time getTerminationTime() {
        return terminationtime;
    }

    public Time getDeadline() {
        return deadline;
    }

    public synchronized void setDeadline(Time t) {
        this.deadline = t;
    }

    public synchronized Time getCreationTime() {
        return creationtime;
    }

    public void setCreationTime(Time t) {
        this.creationtime = t;
    }

    public Collection<Cpu> getCpus() {
        return cpus;
    }
    
    public Collection<Cpu> getCpusSnapshot() {
        synchronized(cpus) {
            return new ArrayList<Cpu>(cpus);
        }
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
    
    public int getDoneJobCount() {
        return doneJobCount;
    }
    
    public void suspend() {
        synchronized(this) {
            suspended = true;
        }
        // ensure we still shut down if no jobs are running
        shutdownIfEmpty(null);
    }

    public boolean isSuspended() {
    	boolean suspended;
    	synchronized(this) {
    	    suspended = this.suspended;
    	}
    	if (suspended) {
    		shutdownIfEmpty(null);
    	}
    	return suspended;
    }

    public synchronized boolean isShutDown() {
        return shutdown;
    }

    public void jobPulled() {
        lastUsed = System.currentTimeMillis();
    }

    public void jobCompleted() {
    	lastUsed = System.currentTimeMillis();
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public int compareTo(Block o) {
        return id.compareTo(o.id);
    }

    public void removeNode(Node node) {
        int left;
        synchronized(cpus) {
            nodes.remove(node);
            for (Cpu cpu : node.getCpus()) {
                scpus.remove(cpu);
                cpus.remove(cpu);
            }
            left = nodes.size();
        }
        bqp.nodeRemoved(node);
    }
}
