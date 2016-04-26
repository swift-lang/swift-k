/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2009-2014 University of Chicago
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

// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.channels.CoasterChannel;

public class BlockQueueProcessor extends AbstractBlockWorkerManager implements Runnable {
    public static final Logger logger = Logger.getLogger(BlockQueueProcessor.class);

    private Settings settings;

    private final Map<Integer, List<Job>> tl;

    /**
       Jobs not yet moved to holding because the allocator was
       planning while it was enqueued
     */
    private final List<Job> incoming;

    /**
       Jobs not moved to queued - they may not fit into existing
       blocks
     */
    private SortedJobSet holding;

    /**
       Jobs that either fit into existing Blocks or were enqueued
       since the last updatePlan
     */
    private final SortedJobSet queued;
    
    /* Need to keep an account of running jobs in order to correctly
     * make sense of the allocated size. If running jobs are not
     * considered, it can appear that the allocated size is much
     * larger than the required size, and blocks get shut down
     * inappropriately.
     */
    private final JobSet running;

    private List<Integer> sums;

    private double allocsize;

    double medianSize, tsum;

    private BQPMonitor monitor;

    private boolean done, planning, shuttingDown; 

    /**
       Formatter for time-based variables in whole seconds
     */
    private static final NumberFormat SECONDS =
    	new DecimalFormat("0");

    /**
       Formatter for time-based variables to thousandth of second
    */
    private static final NumberFormat SECONDS_3 =
    	new DecimalFormat("0.000");

    public BlockQueueProcessor(LocalTCPService localService) {
        super("Block Queue Processor", localService, new Settings(), null);
        this.settings = (Settings) super.getSettings();
        this.setMetric(new OverallocatedJobDurationMetric(this.settings));
        holding = new SortedJobSet();
        tl = new HashMap<Integer, List<Job>>();
        incoming = new ArrayList<Job>();
        queued = new SortedJobSet(getMetric());
        running = new JobSet(getMetric());
        if (logger.isInfoEnabled()) {
            logger.info("Starting... id=" + this.getBQPId());
        }
    }

    @Override
    public void run() {
        try {
            int planTimeMillis = 1;
            while (!done && !shuttingDown) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Holding queue job count: " +
                             holding.size());
                }
                planTimeMillis = updatePlan();
                double planTime = ((double) planTimeMillis) / 1000;
                if (logger.isDebugEnabled()) {
                    logger.debug("Planning time (seconds): " +
                             SECONDS_3.format(planTime));
                }
                if (holding.size() + incoming.size() == 0) {
                    planTimeMillis = 100;
                }
                synchronized (incoming) {
                    incoming.wait(Math.min(planTimeMillis * 20, 10000) + 1000);
                }
            }
            if (shuttingDown) {
                logger.info("Service shutting down. Exiting planning loop.");
            }
        }
        catch (Exception e) {
            CoasterService.error(13, "Exception caught in block processor", e);
        }
    }

    public void add(int time, List<Job> jobs) {
        time += 1;
        tl.put(time, jobs);
        if (time == 0) {
            enqueue(jobs);
        }
    }

    @Override
    public void enqueue(Task t) {
        enqueue1(t);
    }

    public void enqueue1(Task t) {
    	queuedJobs++;
        Job j = new Job(t);
        if (checkJob(j)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Got job with walltime = " + j.getMaxWallTime());
            }
            synchronized (holding) {
                if (planning) {
                    incoming.add(j);
                }
                else {
                    holding.add(j);
                }
            }
        }
    }

    private boolean checkJob(Job job) {
        if (job.getMaxWallTime().getSeconds() > settings.getMaxtime().getSeconds() - settings.getReserve().getSeconds()) {
            job.fail("Task walltime > maxJobTime - reserve (" + 
                    WallTime.format("hms", job.getMaxWallTime().getSeconds()) + " > " + 
                    WallTime.format("hms", settings.getMaxtime().getSeconds() - settings.getReserve().getSeconds()) + ")", null);
            return false;
        }
        else {
            return true;
        }
    }

    public void enqueue(List<Job> jobs) {
        synchronized (incoming) {
            incoming.addAll(jobs);
        }
    }

    private void queue(Job job) {
    	synchronized (queued) {
    	    queued.add(job);
            queued.notify();
        }
    }

    public void waitForJobs() throws InterruptedException {
        synchronized (queued) {
            queued.wait(1000);
        }
    }

    private void cleanDoneBlocks() {
        int count = 0;
        List<Block> snapshot;
        Map<String, Block> blocks = getBlocks();
        synchronized (blocks) {
            snapshot = new ArrayList<Block>(blocks.values());
        }
        for (Block b : snapshot) {
            if (b.isDone()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cleaning done block " + b);
                }
                b.shutdown(false);
                count++;
            }
        }
        if (count > 0) {
            logger.debug("Cleaned " + count + " done blocks");
        }
    }

    private double lastAllocSize;

    private void updateAllocatedSize() {
        Map<String, Block> blocks = getBlocks();
        synchronized (blocks) {
            allocsize = 0;
            for (Block b : blocks.values()) {
                allocsize += b.sizeLeft();
            }
            if (allocsize != lastAllocSize) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Updated allocsize: " + allocsize);
                }
            }
            lastAllocSize = allocsize;
        }
    }

    private boolean fits(Job j) {
        Map<String, Block> blocks = getBlocks();
        synchronized (blocks) {
            for (Block b : blocks.values()) {
                if (!b.isSuspended() && b.fits(j)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void queueToExistingBlocks() {
        List<Job> remove = new ArrayList<Job>();
        double runningSize = getRunningSizeLeft();
        int count = 0;
        for (Job j : holding) {
            if (allocsize - queued.getJSize() - runningSize > getMetric().getSize(j) && fits(j)) {
                queue(j);
                remove.add(j);
                count++;
            }
        }
        if (count > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Queued " + count + " jobs to existing blocks");
            }
        }
        holding.removeAll(remove);
    }

    private void requeueNonFitting() {
        int count = 0;
        double runningSize = getRunningSizeLeft();
        if (logger.isDebugEnabled()) {
            logger.debug("allocsize = " + allocsize +
                     ", queuedsize = " + queued.getJSize() +
                     ", running = " + runningSize +
                     ", qsz = " + queued.size());
        }
        while (allocsize - queued.getJSize() - runningSize < 0) {
            Job j = queued.removeOne(TimeInterval.FOREVER,
                                     Integer.MAX_VALUE);
            if (j == null) {
                if (queued.getJSize() > 0) {
                    CoasterService.error(19, "queuedsize > 0 but no job dequeued. Queued: " + queued,
                        new Throwable());
                }
                else if (allocsize - getRunningSizeLeft() < 0) {
                    warnAboutWalltimes(running);
                }
            }
            else {
                holding.add(j);
                count++;
            }
        }
        if (count > 0) {
            if (logger.isInfoEnabled()) {
                logger.info("Requeued " + count + " non-fitting jobs");
            }
        }
    }
    
    private void warnAboutWalltimes(Iterable<Job> set) {
        synchronized(set) {
            for (Job r : set) {
                if (r.getMaxWallTime().isLessThan(Time.now().subtract(r.getStartTime()))) {
                    Task t = r.getTask();
                    if (t.getAttribute("#warnedAboutWalltime") == null) {
                        logger.warn("The following job exceeded its walltime: " + 
                            t.getSpecification());
                        t.setAttribute("#warnedAboutWalltime", Boolean.TRUE);
                    }
                }
            }
        }
    }

    private double getRunningSizeLeft() {
        synchronized(running) {
            return running.getSizeLeft();
        }
    }

    private void computeSums() {
        sums = new ArrayList<Integer>(holding.size());
        sums.add(0);
        int ps = 0;
        for (Job j : holding) {
            ps += getMetric().getSize(j);
            sums.add(new Integer(ps));
        }
    }

    /**
       Total request size in seconds
     */
    private int computeTotalRequestSize() {
        double sz = 0;
        for (Job j : holding) {
            sz += getMetric().desiredSize(j);
        }
        if (sz > 0) {
            if (sz < 1)
                sz = 1;
            if (logger.isInfoEnabled()) {
                logger.info("Jobs in holding queue: " + holding.size());
            }
            String s = SECONDS.format(sz);
            if (logger.isInfoEnabled()) {
                logger.info("Time estimate for holding queue (seconds): " + s);
            }
        }
        return (int) sz;
    }

    /**
       @return Time overallocation in seconds
     */
    public int overallocatedSize(Job j) {
        return overallocatedSize(j, settings);
    }

    /**
       @return Time overallocation in seconds
     */
    public static int overallocatedSize(Job j, Settings settings) {
        int walltime = (int) j.getMaxWallTime().getSeconds();
        return overallocatedSize(walltime, settings);
    }

    /**
       @param walltime in seconds
       @return Time overallocation in seconds
     */
    private static int overallocatedSize(int walltime, Settings settings) {
        double L = settings.getLowOverallocation();
        double H = settings.getHighOverallocation();
        double D = settings.getOverallocationDecayFactor();
        return (int) (walltime * ((L - H) * Math.exp(-walltime * D) + H));
    }

    private int sumSizes(int start, int end) {
        int sumstart = sums.get(start);
        int sumend = sums.get(end);
        return sumend - sumstart;
    }

    /**
     * Suspends blocks while the total size left in the blocks is
     * larger than the amount of size needed.
     *
     * Blocks with the least amount of size left are suspended first.
     *
     * Blocks are only suspended if both the above size condition is true
     * and they have not seen any work within a certain time interval. This
     * is done to dampen the effects of transients in the submission
     * pattern.
     *
     * Once a block is suspended, it will finish its current tasks and then
     * shut down.
     *
     */
    protected void removeIdleBlocks() {
        SortedMap<Double, Block> sorted = new TreeMap<Double, Block>();
        Map<String, Block> blocks = getBlocks();
        synchronized (blocks) {
            for (Block b : blocks.values()) {
                sorted.put(b.sizeLeft(), b);
            }
        }

        double needed = queued.getJSize() + running.getSize();

        double sum = 0;
        for (Block b : sorted.values()) {
            if (sum >= needed
                    && !b.isSuspended()
                    && (System.currentTimeMillis() - b.getLastUsed()) > Block.SUSPEND_SHUTDOWN_DELAY) {
                b.suspend();
            }
            sum += b.sizeLeft();
        }
    }

    /**
       Move Jobs from {@link holding} to {@link queued} by
       allocating new Blocks for them
     */
    private void allocateBlocks(double tsum) {
        Map<String, Block> blocks = getBlocks();
        List<Job> remove = new ArrayList<Job>();
        // Calculate chunkOfBlocks: how many blocks we may allocate
        //     in this particular call to allocateBlocks()
        int maxBlocks = settings.getMaxBlocks();
        double fraction = settings.getAllocationStepSize();
        int chunkOfBlocks =
            (int) Math.ceil((maxBlocks - blocks.size()) * fraction);

        // Last job queued to last new Block
        int last = 0;
        Iterator<Job> lastI = holding.iterator();
        // Index through holding queue
        int index = 0;
        Iterator<Job> indexI = holding.iterator();
        // Number of new Blocks allocated in this call
        int newBlocks = 0;
        
        int added = 0;

        // get the size (w * h) for the current block by 
        // dividing the total required size (tsum) by the number
        // of slots used in this round and scaling based on the spread.
        //
        // i.e.  0 spread            max spread   
        //    ________________                 ____
        //    |  |  |  |  |  |              ___|  |
        //    |  |  |  |  |  |           ___|  |  |
        //    |  |  |  |  |  |        ___|  |  |  |
        //    |  |  |  |  |  |     ___|  |  |  |  |
        //    |__|__|__|__|__|     |__|__|__|__|__|
        // (where the total area =~ tsum in both cases)
        Metric metric = getMetric();
        double size = metric.blockSize(newBlocks, chunkOfBlocks, tsum);

        String s = SECONDS.format(tsum);
        if (logger.isInfoEnabled()) {
            logger.info("Allocating blocks for a total walltime of: " + s + "s");
        }

        while (indexI.hasNext() && newBlocks < chunkOfBlocks) {
            Job job = indexI.next();
            
            int granularity = settings.getNodeGranularity() * settings.getJobsPerNode();
            // true if the number of jobs for this block is a multiple
            // of granularity
            boolean granularityFit = (index - last) % granularity == 0;
            // true if we've reached the end of the job list
            boolean lastChunk = (index == holding.size() - 1);
            // true when the size of jobs from the last allocated one up to the current one
            // are greater than the size (which means we have to start committing jobs to the block)
            boolean sizeFit = false;
            if (!lastChunk) {
                sizeFit = sumSizes(last, index) > size;
            }
            
            // if there are enough jobs and they match the granularity or if these are the last jobs
            if ((granularityFit && sizeFit) || lastChunk) {
                int msz = (int) size;
                // jobs are sorted on walltime, and the last job is the longest,
                // so use that for calculating the overallocation
                int lastwalltime = (int) job.getMaxWallTime().getSeconds();
                int h = overallocatedSize(job);
                
                // the maximum time is a hard limit, so for the maximum useable time
                // the reserve needs to be subtracted
                int maxt =
                  (int) settings.getMaxtime().getSeconds() - (int) settings.getReserve().getSeconds();
                // height must be a multiple of the overallocation of the
                // largest job
                // Is not h <= round(h, lastwalltime) ? -Justin
                //   Yes, it is (see comment in round()) - Mihael
                // h = Math.min(Math.max(h, round(h, lastwalltime)), maxt);
                
                // If h > maxt, should we report a warning/error? -Justin
                //   No. h is the overallocated time (i.e. greater than the
                //   job walltime). So it's acceptable for that to go over maxt.
                //   The error is when walltime > maxt - Mihael
                h = Math.min(round(h, lastwalltime), maxt);
                
                // once we decided on a height, get the width by dividing the total size
                // by the height (and rounding appropriately to fit the granularity), 
                // while making sure that we don't go over maxNodes
                int width =
                        Math.min(round(metric.width(msz, h), granularity),
                                 settings.getMaxNodes()
                                 * settings.getJobsPerNode());
                // while we were shooting to have the number of jobs be a multiple of the
                // width, various constraints might have changed that, so adjust the 
                // number of jobs accordingly
                int r = (index - last) % width;

                if (logger.isInfoEnabled()) {
                	logger.info("\t Considering: " + job);
                	logger.info("\t  Max Walltime (seconds):   " + lastwalltime);
                    logger.info("\t  Time estimate (seconds):  " + h);
                    logger.info("\t  Total for this new Block (est. seconds): " +
                                sumSizes(last, index));
                }

                // read more jobs to fill up the remaining space towards the granularity
                // +-----+     +-----+
                // |xx...|     |xxrrr|
                // |xxxxx| --> |xxxxx|
                // |xxxxx|     |xxxxx|
                // |xxxxx|     |xxxxx|
                // +-----+     +-----+
                
                
                if (r != 0) {
                	// and make sure that there is enough walltime to run the new added jobs
                	// though this is improper: what should be added to h is 
                	// (newLastwalltime - lastwalltime) where newLastwalltime is the walltime
                	// of the i-th job after adding (w - r).
                    h = Math.min(h + lastwalltime, maxt);
                }

                // create the actual block
                Block b = new Block(width, TimeInterval.fromSeconds(h), settings.getMaxWorkerIdleTime(), this);
                getLocalService().registerBlock(b, this);
                if (logger.isInfoEnabled()) {
                    logger.info("index: " + index + ", last: " + last + ", holding.size(): " + holding.size());
                }
                // now add jobs from holding until the size of the jobs exceeds the
                // size of the block (as given by the metric)
                int ii = last;
                while (lastI.hasNext() && sumSizes(last, ii + 1) <= metric.size(width, h)) {
                    Job j = lastI.next();
                    queue(j);
                    remove.add(j);
                    added++;
                    ii++;
                }
                // lastI  <-> ii
                // indexI <-> index + 1
                // if ii == index + 1 <=> ii - index - 1 == 0 then 
                //     lastI and indexI point to the same element
                
                
                if (logger.isInfoEnabled()) {
                    logger.info("Queued: " + (ii-last) + " jobs to new Block");
                    logger.info("index: " + index + ", last: " + last + ", ii: " + ii + ", holding.size(): " + holding.size());
                }
                // update index of last added job
                // skip ii - index - 1 jobs since the iterator and "index" are off by one
                // since index only gets updated at the end of the loop
                for (int i = 0; i < ii - index - 1; i++) {
                    indexI.next();
                }
                index = ii - 1;
                // commit the block
                addBlock(b);
                last = index + 1;

                newBlocks++;
                size = metric.blockSize(newBlocks, chunkOfBlocks, tsum);
            }
            index++;
        }
        holding.removeAll(remove);
        if (added > 0) {
            if (logger.isInfoEnabled()) {
                logger.info("Added " + added + " jobs to new blocks");
            }
        }
    }


    private void commitNewJobs() {
        synchronized (incoming) {
            holding.addAll(incoming);
            if (incoming.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Committed " + incoming.size() + " new jobs");
                }
            }
            incoming.clear();
        }
    }

    private long lastUpdate;

    private void updateMonitor() {
        if (settings.isRemoteMonitorEnabled()) {
            long now = System.currentTimeMillis();
            if (now - lastUpdate > 10000) {
                if (monitor == null) {
                    monitor = new RemoteBQPMonitor(this);
                }
                monitor.update();
                lastUpdate = now;
            }
        }
    }
    
    /**
      @return Time consumed in milliseconds
     */
    public int updatePlan() throws PlanningException {
        Set<Job> tmp;

        long start = System.currentTimeMillis();

        synchronized(holding) {
            planning = true;
        }

        // Shutdown Blocks that are done
        cleanDoneBlocks();

        // Subtract elapsed time from existing allocation
        updateAllocatedSize();
    
        // Move jobs that fit from holding to queued
        queueToExistingBlocks();

        // int jss = jobs.size();
        // If queued has too many Jobs, move some back to holding
        requeueNonFitting();

        computeSums();

        tsum = computeTotalRequestSize();

        if (tsum == 0) {
            removeIdleBlocks();
        }
        else {
            allocateBlocks(tsum);
        }

        synchronized(holding) {
            planning = false;
            // Move all incoming Jobs to holding
            commitNewJobs();
        }

        updateMonitor();
        
        return (int) (System.currentTimeMillis() - start);
    }

    public Job request(Cpu who, TimeInterval ti, int cpus, boolean allowShutdownSignal) {
        Job job = queued.removeOne(ti, cpus);
        if (job == null) {
            synchronized(holding) {
                if (!planning) {
                    job = holding.removeOne(ti, cpus);
                }
            }
        }
        
        if (job != null) {
        	queuedJobs--;
            synchronized(running) {
                running.add(job);
                runningJobs = running.size();
            }
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("request - no job " + ti + ", " + cpus);
            }
        }
        return job;
    }

    @Override
    public void jobTerminated(Job job) {
        synchronized(running) {
        	runningJobs--;
            running.remove(job);
        }
    }

    /**
       Round v up to the next multiple of g
     */
    private int round(int v, int g) {
        // (v % g) < g => x - (v % g) + g > x
        int r = v - (v % g) + g;
        return r;
    }

    // private static int jid;

    public void addToPlannedQueue(Task t) {
        queue(new Job(t));
    }

    @Override
    public void startShutdown() {
        synchronized(holding) {
            shuttingDown = true;
        }
        super.startShutdown();
        done = true;
    }
    
    public boolean isShutDown() {
        return getBlocks().size() == 0;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public List<Job> getJobs() {
        return holding.getAll();
    }

    public SortedJobSet getQueued() {
        return queued;
    }

    public static void main(String[] args) {
        Settings s = new Settings();
        System.out.println(overallocatedSize(1, s));
        System.out.println(overallocatedSize(10, s));
        System.out.println(overallocatedSize(100, s));
        System.out.println(overallocatedSize(1000, s));
        System.out.println(overallocatedSize(3600, s));
        System.out.println(overallocatedSize(10000, s));
        System.out.println(overallocatedSize(100000, s));
    }

    @Override
    public int getQueueSeq() {
        return queued.getSeq() + holding.getSeq();
    }

    @Override
    public void cancelTasksForChannel(CoasterChannel channel, String taskId) {
        String channelId = channel.getID();
        cancelTasks(holding, channelId, taskId);
        cancelTasks(queued, channelId, taskId);
        cancelRunningTasks(running, channelId, taskId);
    }

    private void cancelTasks(SortedJobSet s, String channelId, String taskId) {
        synchronized(s) {
            List<Job> l = s.getAll();
            for (Job j : l) {
                if (!matchesTaskId(taskId, j)) {
                    continue;
                }
                String taskChannelId = (String) j.getTask().getAttribute("channelId");
                if (channelId.equals(taskChannelId)) {
                    s.remove(j);
                    NotificationManager.getDefault().removeTask(j.getTask());
                }
            }
        }
    }
    
    private void cancelRunningTasks(JobSet s, String channelId, String taskId) {
        synchronized(s) {
            for (Job j : s) {
                if (!matchesTaskId(taskId, j)) {
                    continue;
                }
                String taskChannelId = (String) j.getTask().getAttribute("channelId");
                if (channelId.equals(taskChannelId)) {
                    j.cancel();
                }
            }
        }
    }
}
