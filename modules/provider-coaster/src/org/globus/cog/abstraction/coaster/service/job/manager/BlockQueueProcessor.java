package org.globus.cog.abstraction.coaster.service.job.manager;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.coaster.service.RegistrationManager;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public class BlockQueueProcessor extends AbstractQueueProcessor implements RegistrationManager,
        Runnable {
    public static final Logger logger = Logger.getLogger(BlockQueueProcessor.class);

    private Settings settings;

    private Map tl;
    private List jobs, add;
    private SortedJobSet queued;
    private List sums;
    private List blocks;

    private double allocsize;

    double medianSize, tsum;

    private File script;

    private String id;

    private static final DateFormat DDF = new SimpleDateFormat("MMdd-mmhhss");

    private BQPMonitor monitor;

    private ChannelContext clientChannelContext;

    private boolean done, planning;

    private Metric metric;

    public BlockQueueProcessor() {
        super("Block Queue Processor");
        jobs = new ArrayList();
        blocks = new ArrayList();
        tl = new HashMap();
        settings = new Settings();
        id = DDF.format(new Date());
        add = new ArrayList();
        metric = new OverallocatedJobDurationMetric(settings);
        queued = new SortedJobSet(metric);
    }

    public Metric getMetric() {
        return metric;
    }

    public void run() {
        try {
            script = ScriptManager.writeScript();
            int planTime = 1;
            while (!done) {
                planTime = updatePlan();
                logger.info("Plan time: " + planTime);
                if (jobs.size() + add.size() == 0) {
                    planTime = 100;
                }
                synchronized (add) {
                    add.wait(Math.min(planTime * 20, 10000) + 200);
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

    public void add(int time, List jobs) {
        time += 1;
        tl.put(new Integer(time), jobs);
        if (time == 0) {
            enqueue(jobs);
        }
    }

    public void enqueue(Task t) {
        enqueue1(t);
    }

    public void enqueue1(Task t) {
        synchronized (add) {
            Job j = new Job(t);
            if (logger.isInfoEnabled()) {
                logger.info("Got job with walltime = " + j.getMaxWallTime());
            }
            if (planning) {
                add.add(j);
            }
            else {
                queue(j);
            }
        }
    }

    public void enqueue(List jobs) {
        synchronized (add) {
            add.addAll(jobs);
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
        synchronized (blocks) {
            int count = 0;
            Iterator ib = blocks.iterator();
            while (ib.hasNext()) {
                Block b = (Block) ib.next();
                if (b.isDone()) {
                    b.shutdown();
                    count++;
                }
            }
            if (count > 0) {
                logger.info("Cleaned " + count + " done blocks");
            }
        }
    }

    private double lastAllocSize;
    private void updateAllocatedSize() {
        synchronized (blocks) {
            allocsize = 0;
            Iterator i = blocks.iterator();
            while (i.hasNext()) {
                Block b = (Block) i.next();
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
            Iterator i = blocks.iterator();
            while (i.hasNext()) {
                Block b = (Block) i.next();
                if (b.fits(j)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void addBlock(Block b) {
        synchronized (blocks) {
            blocks.add(b);
        }
        b.start();
    }

    private Set queueToExistingBlocks() {
        Set remove = new HashSet();
        Iterator it = jobs.iterator();
        while (it.hasNext()) {
            Job j = (Job) it.next();
            if (allocsize - queued.getJSize() > j.getMaxWallTime().getSeconds() && fits(j)) {
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
            Job j = queued.removeOne(TimeInterval.FOREVER);
            if (j == null) {
                CoasterService.error(19, "queuedsize > 0 but no job dequeued. Queued: " + queued,
                    new Throwable());
            }
            jobs.add(j);
            count++;
        }
        if (count > 0) {
            logger.info("Requeued " + count + " non-fitting jobs");
        }
    }

    private void computeSums() {
        sums = new ArrayList();
        sums.add(new Integer(0));
        int ps = 0;
        Iterator i = jobs.iterator();
        while (i.hasNext()) {
            Job j = (Job) i.next();
            ps += metric.getSize(j);
            sums.add(new Integer(ps));
        }
    }

    private int computeTotalRequestSize() {
        int sz = 0;
        Iterator i = jobs.iterator();
        while (i.hasNext()) {
            Job j = (Job) i.next();
            sz += metric.desiredSize(j);
        }
        if (sz > 0) {
            logger.info("Required size: " + sz + " for " + jobs.size() + " jobs");
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
        int os =
                (int) (wt * ((settings.getLowOverallocation() - settings.getHighOverallocation())
                        * Math.exp(-wt * settings.getOverallocationDecayFactor()) + settings.getHighOverallocation()));
        if (logger.isDebugEnabled()) {
            logger.debug("overallocatedSize(" + wt + ") = " + os);
        }
        return os;
    }

    private int sumSizes(int start, int end) {
        int sumstart = ((Integer) sums.get(start)).intValue();
        int sumend = ((Integer) sums.get(end)).intValue();
        return sumend - sumstart;
    }

    private Job getJob(int index) {
        return (Job) jobs.get(index);
    }

    private Set allocateBlocks(double tsum) {
        Set remove = new HashSet();
        int cslots =
                (int) Math.ceil((settings.getSlots() - blocks.size())
                        * settings.getAllocationStepSize());

        int last = 0;
        int i = 0;
        int slot = 0;

        double size = metric.blockSize(slot, cslots, tsum);

        while (i <= jobs.size() && slot < cslots) {
            int granularity = settings.getNodeGranularity() * settings.getWorkersPerNode();
            boolean granularityFit = (i - last) % granularity == 0;
            boolean lastChunk = i == jobs.size() - 1;
            boolean sizeFit = false;
            if (!lastChunk) {
                sizeFit = sumSizes(last, i) > size;
            }
            if ((granularityFit && sizeFit) || lastChunk) {
                int msz = (int) size;
                int lastwalltime = (int) getJob(i).getMaxWallTime().getSeconds();
                int h = overallocatedSize(getJob(i));
                // height must be a multiple of the overallocation of the
                // largest job
                h = Math.min(Math.max(h, round(h, lastwalltime)), settings.getMaxtime());
                int w = Math.min(round(metric.width(msz, h), granularity), settings.getMaxNodes() * settings.getWorkersPerNode());
                int r = (i - last) % w;
                if (logger.isInfoEnabled()) {
                    logger.info("h: " + h + ", jj: " + lastwalltime + ", x-last: " + ", r: " + r
                            + ", sumsz: " + sumSizes(last, i));
                }

                // readjust number of jobs fitted based on granularity
                i += (w - r);
                if (r != 0) {
                    h += lastwalltime;
                }

                if (logger.isInfoEnabled()) {
                    logger.info("h: " + h + ", w: " + w + ", size: " + size + ", msz: " + msz
                            + ", w*h: " + w * h);
                }

                Block b = new Block(w, TimeInterval.fromSeconds(h), this);
                int ii = last;
                while (ii < jobs.size() && sumSizes(last, ii + 1) <= metric.size(w, h)) {
                    queue(getJob(ii));
                    remove.add(getJob(ii));
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

    private void removeJobs(Set r) {
        List old = jobs;
        jobs = new ArrayList();
        Iterator i = old.iterator();
        while (i.hasNext()) {
            Job j = (Job) i.next();
            if (!r.contains(j)) {
                jobs.add(j);
            }
        }
    }

    private boolean first = true;

    private void updateSettings() throws PlanningException {
        if (!jobs.isEmpty()) {
            Job j = (Job) jobs.get(0);
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
        synchronized (add) {
            jobs.addAll(add);
            if (add.size() > 0) {
                logger.info("Committed " + add.size() + " new jobs");
            }
            add.clear();
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
        synchronized (add) {
            planning = true;
        }
        long start = System.currentTimeMillis();
        commitNewJobs();
        cleanDoneBlocks();
        updateAllocatedSize();

        removeJobs(queueToExistingBlocks());

        int jss = jobs.size();
        requeueNonFitting();
        updateSettings();

        computeSums();

        tsum = computeTotalRequestSize();

        removeJobs(allocateBlocks(tsum));

        updateMonitor();
        synchronized (add) {
            planning = false;
        }
        return (int) (System.currentTimeMillis() - start);
    }

    public Job request(TimeInterval ti) {
        return queued.removeOne(ti);
    }

    private int round(int v, int g) {
        int r = v - (v % g) + g;
        return r;
    }

    private static int jid;

    public void addToPlannedQueue(Task t) {
        queue(new Job(t));
    }

    public void shutdown() {
        shutdownBlocks();
        done = true;
    }

    private void shutdownBlocks() {
        logger.info("Shutting down blocks");
        synchronized (blocks) {
            Iterator i = new ArrayList(blocks).iterator();
            while (i.hasNext()) {
                Block b = (Block) i.next();
                b.shutdown();
            }
        }
    }

    public void blockTaskFinished(Block block) {
        if (logger.isInfoEnabled()) {
            logger.info("Removing block " + block);
        }
        synchronized (blocks) {
            blocks.remove(block);
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    private Block getBlock(String id) {
        synchronized (blocks) {
            Iterator i = blocks.iterator();
            while (i.hasNext()) {
                Block b = (Block) i.next();
                if (b.getId().equals(id)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("No such block: " + id);
        }
    }

    public String registrationReceived(String bid, String id, ChannelContext channelContext) {
        return getBlock(bid).cpuStarted(id, channelContext);
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

    public List getJobs() {
        return jobs;
    }

    public SortedJobSet getQueued() {
        return queued;
    }

    public List getBlocks() {
        return blocks;
    }

    public void setClientChannelContext(ChannelContext channelContext) {
        this.clientChannelContext = channelContext;
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
}
