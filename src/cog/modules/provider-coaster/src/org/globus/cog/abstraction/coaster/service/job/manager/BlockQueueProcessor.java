package org.globus.cog.abstraction.coaster.service.job.manager;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.rlog.RemoteLogger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.coaster.service.RegistrationManager;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public class BlockQueueProcessor extends AbstractQueueProcessor implements RegistrationManager,
        Runnable {
    public static final Logger logger = Logger.getLogger(BlockQueueProcessor.class);

    private Settings settings;

    private Map<Integer, List<Job>> tl;
    
    /** 
       Jobs not yet moved to holding because the allocator was 
       planning while it was enqueued 
     */
    private List<Job> incoming;
    
    /** 
       Jobs not moved to queued - they may not fit into existing 
       blocks   
     */
    private List<Job> holding;
    
    /** 
       Jobs that either fit into existing Blocks or were enqueued 
       since the last updatePlan 
     */
    private SortedJobSet queued;
    
    private List<Integer> sums;
    private Map<String, Block> blocks;

    private double allocsize;

    double medianSize, tsum;

    private File script;

    private String id;

    private static final DateFormat DDF = new SimpleDateFormat("MMdd-mmhhss");

    private BQPMonitor monitor;

    private ChannelContext clientChannelContext;

    private boolean done, planning;

    private Metric metric;

    private final RemoteLogger rlogger;

    public BlockQueueProcessor(Settings settings) {
        super("Block Queue Processor");
        this.settings = settings;
        holding = new ArrayList<Job>();
        blocks = new TreeMap<String, Block>();
        tl = new HashMap<Integer, List<Job>>();
        id = DDF.format(new Date());
        incoming = new ArrayList<Job>();
        metric = new OverallocatedJobDurationMetric(settings);
        queued = new SortedJobSet(metric);
        rlogger = new RemoteLogger();
    }

    public Metric getMetric() {
        return metric;
    }

    @Override
    public void run() {
        try {
            script = ScriptManager.writeScript();
            int planTime = 1;
            while (!done) {
                logger.debug("Plan: holding.size(): " + holding.size());
                planTime = updatePlan();
                logger.info("Plan time: " + planTime);
                if (holding.size() + incoming.size() == 0) {
                    planTime = 100;
                }
                synchronized (incoming) {
                    incoming.wait(Math.min(planTime * 20, 10000) + 200);
                }
            }
        }
        catch (Exception e) {
            CoasterService.error(13, "Exception caught in block processor", e);
        }
    }

    public File getScript() {
        return script;
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
        synchronized (incoming) {
            Job j = new Job(t);
            if (logger.isDebugEnabled()) {
                logger.debug("Got job with walltime = " + j.getMaxWallTime());
            }
            if (planning) {
                incoming.add(j);
            }
            else {
                queue(j);
            }
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
        synchronized (blocks) {
            snapshot = new ArrayList<Block>(blocks.values());
        }
        for (Block b : snapshot) {
            if (b.isDone()) {
                b.shutdown(false);
                count++;
            }
        }
        if (count > 0) {
            logger.info("Cleaned " + count + " done blocks");
        }
    }

    private double lastAllocSize;

    private void updateAllocatedSize() {
        synchronized (blocks) {
            allocsize = 0;
            for (Block b : blocks.values()) {
                allocsize += b.sizeLeft();
            }
            if (allocsize != lastAllocSize) {
                logger.info("Updated allocsize: " + allocsize);
            }
            lastAllocSize = allocsize;
        }
    }

    private boolean fits(Job j) {
        synchronized (blocks) {
            for (Block b : blocks.values()) {
                if (!b.isSuspended() && b.fits(j)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void addBlock(Block b) {
        synchronized (blocks) {
            blocks.put(b.getId(), b);
        }
        b.start();
    }

    private Set<Job> queueToExistingBlocks() {
        Set<Job> remove = new HashSet<Job>();
        for (Job j : holding) {
            if (allocsize - queued.getJSize() > metric.getSize(j) && fits(j)) {
                queue(j);
                remove.add(j);
            }
        }
        if (remove.size() > 0) {
            logger.info("Queued " + remove.size() + " jobs to existing blocks");
        }
        return remove;
    }

    private void requeueNonFitting() {
        int count = 0;
        logger.info("allocsize = " + allocsize + ", queuedsize = " + queued.getJSize() + ", qsz = "
                + queued.size());
        while (allocsize - queued.getJSize() < 0) {
            Job j = queued.removeOne(TimeInterval.FOREVER, 
                                     Integer.MAX_VALUE);
            if (j == null) {
                CoasterService.error(19, "queuedsize > 0 but no job dequeued. Queued: " + queued,
                    new Throwable());
            }
            holding.add(j);
            count++;
        }
        if (count > 0) {
            logger.info("Requeued " + count + " non-fitting jobs");
        }
    }

    private void computeSums() {
        sums = new ArrayList<Integer>();
        sums.add(0);
        int ps = 0;
        for (Job j : holding) {
            ps += metric.getSize(j);
            sums.add(new Integer(ps));
        }
    }

    private int computeTotalRequestSize() {
        int sz = 0;
        for (Job j : holding) {
            sz += metric.desiredSize(j);
        }
        if (sz > 0) {
            logger.info("Required size: " + sz + " for " + holding.size() + " jobs");
        }
        return sz;
    }

    public int overallocatedSize(Job j) {
        return overallocatedSize(j, settings);
    }

    public static int overallocatedSize(Job j, Settings settings) {
        return overallocatedSize((int) j.getMaxWallTime().getSeconds(), settings);
    }

    private static int overallocatedSize(int wt, Settings settings) {
        return (int) (wt * ((settings.getLowOverallocation() - settings.getHighOverallocation())
                        * Math.exp(-wt * settings.getOverallocationDecayFactor()) + settings.getHighOverallocation()));
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
     * and they have not see any work withing a certain time interval. This
     * is done to dampen the effects of transients in the submission
     * pattern.
     * 
     * Once a block is suspended, it will finish its current tasks and then
     * shut down.
     * 
     */
    protected void removeIdleBlocks() {
        ArrayList<Block> sorted;
        
        synchronized (blocks) {
            sorted = new ArrayList<Block>(blocks.values());
            Collections.sort(sorted, new Comparator<Block>() {
                public int compare(Block b1, Block b2) {
                    double s1 = b1.sizeLeft();
                    double s2 = b2.sizeLeft();
                    if (s1 < s2) {
                        return -1;
                    }
                    else {
                        return 1;
                    }
                }
            });
        }

        double needed = queued.getJSize();

        double sum = 0;
        for (Block b : sorted) {
            if (sum >= needed
                    && !b.isSuspended()
                    && (System.currentTimeMillis() - b.getLastUsed()) > Block.SUSPEND_SHUTDOWN_DELAY) {
                b.suspend();
            }
            sum += b.sizeLeft();
        }
    }

    private Set<Job> allocateBlocks(double tsum) {
        Set<Job> remove = new HashSet<Job>();
        int cslots =
                (int) Math.ceil((settings.getSlots() - blocks.size())
                        * settings.getAllocationStepSize());

        int last = 0;
        int i = 0;
        int slot = 0;

        double size = metric.blockSize(slot, cslots, tsum);

        while (i <= holding.size() && slot < cslots) {
            int granularity = settings.getNodeGranularity() * settings.getWorkersPerNode();
            boolean granularityFit = (i - last) % granularity == 0;
            boolean lastChunk = i == holding.size() - 1;
            boolean sizeFit = false;
            if (!lastChunk) {
                sizeFit = sumSizes(last, i) > size;
            }
            if ((granularityFit && sizeFit) || lastChunk) {
                int msz = (int) size;
                int lastwalltime = (int) holding.get(i).getMaxWallTime().getSeconds();
                int h = overallocatedSize(holding.get(i));
                // height must be a multiple of the overallocation of the
                // largest job
                int maxt = settings.getMaxtime() - (int) settings.getReserve().getSeconds();
                h = Math.min(Math.max(h, round(h, lastwalltime)), maxt);
                int w =
                        Math.min(round(metric.width(msz, h), granularity), settings.getMaxNodes()
                                * settings.getWorkersPerNode());
                int r = (i - last) % w;
                if (logger.isInfoEnabled()) {
                    logger.info("h: " + h + ", jj: " + lastwalltime + ", x-last: " + ", r: " + r
                            + ", sumsz: " + sumSizes(last, i));
                }

                // read just number of jobs fitted based on granularity
                i += (w - r);
                if (r != 0) {
                    h = Math.min(h + lastwalltime, maxt);
                }

                if (logger.isInfoEnabled()) {
                    logger.info("h: " + h + ", w: " + w + ", size: " + size + ", msz: " + msz
                            + ", w*h: " + w * h);
                }

                Block b = new Block(w, TimeInterval.fromSeconds(h), this);
                int ii = last;
                while (ii < holding.size() && sumSizes(last, ii + 1) <= metric.size(w, h)) {
                    queue(holding.get(ii));
                    remove.add(holding.get(ii));
                    ii++;
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Added: " + last + " - " + (ii - 1));
                }
                i = ii - 1;
                addBlock(b);
                last = i + 1;
                slot++;
                size = metric.blockSize(slot, cslots, tsum);
            }
            i++;
        }
        if (remove.size() > 0) {
            logger.info("Added " + remove.size() + " jobs to new blocks");
        }
        return remove;
    }

    private void removeJobs(Set<Job> r) {
        List<Job> old = holding;
        holding = new ArrayList<Job>();
        for (Job j : old) {
            if (!r.contains(j)) {
                holding.add(j);
            }
        }
    }

    private boolean first = true;

    private void updateSettings() throws PlanningException {
        if (!holding.isEmpty()) {
            Job j = holding.get(0);
            Task t = j.getTask();
            ExecutionService p = (ExecutionService) t.getService(0);
            settings.setServiceContact(p.getServiceContact());
            String jm = p.getJobManager();
            int colon = jm.indexOf(':');
            // remove provider used to bootstrap coasters
            jm = jm.substring(colon + 1);
            colon = jm.indexOf(':');
            if (colon == -1) {
                settings.setProvider(jm);
            }
            else {
                settings.setJobManager(jm.substring(colon + 1));
                settings.setProvider(jm.substring(0, colon));
            }
            if (p.getSecurityContext() != null) {
                settings.setSecurityContext(p.getSecurityContext());
            }
            else {
                try {
                    settings.setSecurityContext(AbstractionFactory.newSecurityContext(settings.getProvider()));
                }
                catch (Exception e) {
                    throw new PlanningException(e);
                }
            }
            if (first) {
                logger.info("\n" + settings.toString());
                first = false;
            }
        }
    }

    private void commitNewJobs() {
        synchronized (incoming) {
            holding.addAll(incoming);
            if (incoming.size() > 0) {
                logger.info("Committed " + incoming.size() + " new jobs");
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

    public int updatePlan() throws PlanningException {
        Set<Job> tmp;
        
        synchronized (incoming) {
            planning = true;
        }
        long start = System.currentTimeMillis();
        
        // Move all Jobs in add to jobs
        commitNewJobs();

        // Shutdown Blocks that are done
        cleanDoneBlocks();

        // Subtract elapsed time from existing allocation
        updateAllocatedSize();

        // Move Jobs from jobs that fit to queued
        tmp = queueToExistingBlocks();
        
        // Subtract these Jobs from jobs
        removeJobs(tmp);

        // int jss = jobs.size();
        // If queued has too many Jobs, move some back to jobs
        requeueNonFitting();

        updateSettings();

        computeSums();

        tsum = computeTotalRequestSize();

        if (tsum == 0) {
            removeIdleBlocks();
        }
        else {
            tmp = allocateBlocks(tsum);
            removeJobs(tmp);
        }

        updateMonitor();
        synchronized (incoming) {
            planning = false;
        }
        return (int) (System.currentTimeMillis() - start);
    }

    public Job request(TimeInterval ti, int cpus) {
        return queued.removeOne(ti, cpus);
    }

    private int round(int v, int g) {
        int r = v - (v % g) + g;
        return r;
    }

    // private static int jid;

    public void addToPlannedQueue(Task t) {
        queue(new Job(t));
    }

    @Override
    public void shutdown() {
        shutdownBlocks();
        done = true;
    }

    private void shutdownBlocks() {
        logger.info("Shutting down blocks");
        synchronized (blocks) {
            for (Block b : new ArrayList<Block>(blocks.values())) {
                b.shutdown(true);
            }
        }
    }

    public void blockTaskFinished(Block block) {
        if (logger.isInfoEnabled()) {
            logger.info("Removing block " + block);
        }
        synchronized (blocks) {
            blocks.remove(block.getId());
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    protected Block getBlock(String id) {
        synchronized (blocks) {
            Block b = blocks.get(id);
            if (b != null) {
                return b;
            }
            throw new IllegalArgumentException("No such block: " + id);
        }
    }

    public String registrationReceived(String bid, String id, ChannelContext channelContext) {
        return getBlock(bid).workerStarted(id, channelContext);
    }

    public String nextId(String id) {
        return getBlock(id).nextId();
    }

    public String getBQPId() {
        return id;
    }

    public void setBQPId(String id) {
        this.id = id;
    }

    public List<Job> getJobs() {
        return holding;
    }

    public SortedJobSet getQueued() {
        return queued;
    }

    public Map<String, Block> getBlocks() {
        return blocks;
    }

    public void setClientChannelContext(ChannelContext channelContext) {
        this.clientChannelContext = channelContext;
        rlogger.setChannelContext(channelContext);
    }

    public ChannelContext getClientChannelContext() {
        return clientChannelContext;
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

    public int getQueueSeq() {
        return queued.getSeq();
    }

    public RemoteLogger getRLogger() {
        return rlogger;
    }

    /** 
       Get the KarajanChannel for the worker with given id
     */
    public KarajanChannel getWorkerChannel(String id) {
        
        return null;
    }
}
