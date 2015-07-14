//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 6, 2015
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.rlog.RemoteLogger;
import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.coaster.service.RegistrationManager;
import org.globus.cog.coaster.channels.CoasterChannel;

public abstract class AbstractBlockWorkerManager extends AbstractQueueProcessor implements RegistrationManager {
    public static final Logger logger = Logger.getLogger(AbstractBlockWorkerManager.class);
    
    private final Map<String, Block> blocks;
    
    private final RemoteLogger rlogger;
    
    private Broadcaster broadcaster;
    
    private BaseSettings settings;
    
    private Metric metric;
    
    private PullThread taskDispatcher;
    
    private static final DateFormat DDF = new SimpleDateFormat("MMdd-mmhhss");
    private String id;
    
    private static int sid;
    
    public static volatile int queuedJobs, runningJobs;
    
    private synchronized static int nextSid() {
        return sid++;
    }

    public AbstractBlockWorkerManager(String name, LocalTCPService localService, BaseSettings settings, Metric metric) {
        super(name, localService);
        this.settings = settings;
        this.metric = metric;
        rlogger = new RemoteLogger();
        blocks = new TreeMap<String, Block>();
        id = DDF.format(new Date()) + nextSid();
        taskDispatcher = new PullThread(this);
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

    public Map<String, Block> getBlocks() {
        return blocks;
    }
    
    /**
     * Returns a thread-safe list of blocks known at the time
     * of the call to this function.
     */
    public List<Block> getAllBlocks() {
        synchronized(blocks) {
            return new ArrayList<Block>(blocks.values());
        }
    }
    
    public void addBlock(Block b) {
        synchronized (blocks) {
            blocks.put(b.getId(), b);
        }
        b.start();
    }
    
    protected void shutdownBlocks() {
        logger.info("Shutting down blocks");
        List<Block> bl;
        synchronized (blocks) {
            bl = new ArrayList<Block>(blocks.values());
        }
        for (Block b : bl) {
            b.shutdown(true);
        }
    }
    
    public void blockTaskFinished(Block block) {
        if (logger.isInfoEnabled()) {
            logger.info("Removing block " + block + ". Blocks active: " + blocks.size());
        }
        synchronized (blocks) {
            blocks.remove(block.getId());
        }
        getLocalService().unregisterBlock(block);
    }
    
    public String nextId(String id) {
        return getBlock(id).nextId();
    }
    
    public void setBroadcaster(Broadcaster b) {
        this.broadcaster = b;
        rlogger.setBroadcaster(b);
    }

    public Broadcaster getBroadcaster() {
        return broadcaster;
    }
    
    public RemoteLogger getRLogger() {
        return rlogger;
    }
    
    public String registrationReceived(String blockID, String workerID, String workerHostname,
            CoasterChannel channel, Map<String, String> options) {
        return getBlock(blockID).workerStarted(workerID, workerHostname, channel, options);
    }
    
    public void nodeRemoved(Node node) {
    }
    
    public String getBQPId() {
        return id;
    }

    public void setBQPId(String id) {
        this.id = id;
    }

    public BaseSettings getSettings() {
        return settings;
    }
    
    public Metric getMetric() {
        return metric;
    }
    
    protected void setMetric(Metric metric) {
        this.metric = metric;
    }
    

    @Override
    public void startShutdown() {
        taskDispatcher.shutDown();
        shutdownBlocks();
        super.startShutdown();
    }

    /**
       Get the KarajanChannel for the worker with given id
     */
    public CoasterChannel getWorkerChannel(String blockID, String workerID) {        
        Block b = getBlock(blockID);
        if (b != null) {
            Node n = b.findNode(workerID);
            if (n != null) {
                return n.getChannel();
            }
        }
        return null;
    }
    
    public PullThread getTaskDispatcher() {
        return taskDispatcher;
    }
    
    public void jobTerminated(Job job) {
    }
    
    public abstract Job request(Cpu who, TimeInterval ti, int cpus, boolean allowShutdownSignal) throws BlockShutDownSignal;

    @Override
    public synchronized void start() {
        super.start();
        taskDispatcher.start();
    }

    public Map<String, Object> getMonitoringData() {
        return Collections.emptyMap();
    }
}
