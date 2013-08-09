//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 7, 2013
 */
package org.griphyn.vdl.karajan.monitor.processors.coasters;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.items.AbstractStatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;

public class CoasterStatusItem extends AbstractStatefulItem {
    public static final String ID = "coaster-status";
    
    private Map<String, Block> blocks;
    private int queuedBlocks, activeBlocks, doneBlocks, failedBlocks;
    private int requestedCores, activeCores, doneCores, failedCores;
    
    public CoasterStatusItem() {
        super(ID);
        blocks = new HashMap<String, Block>();
    }

    @Override
    public StatefulItemClass getItemClass() {
        return StatefulItemClass.MISC;
    }
    
    public synchronized void newBlock(String id, int cores, int coresPerWorker) {
        Block b = new Block(id, cores, coresPerWorker);
        blocks.put(id, b);
        queuedBlocks++;
        requestedCores += cores;
    }
    
    public synchronized void blockActive(String id) {
        getBlock(id).state = BlockState.ACTIVE;
        activeBlocks++;
        queuedBlocks--;
    }
    
    public synchronized void blockFailed(String id) {
        Block b = getBlock(id);
        if (b.state == BlockState.ACTIVE) {
            activeBlocks--;
        }
        else {
            queuedBlocks--;
        }
        b.state = BlockState.FAILED;
        activeCores -= b.activeCores;
        b.activeCores = 0;
        failedBlocks++;
    }
    
    public synchronized void blockDone(String id) {
        Block b = getBlock(id);
        activeBlocks--;
        b.state = BlockState.DONE;
        activeCores -= b.activeCores;
        b.activeCores = 0;
        doneBlocks++;
    }
    
    public synchronized void workerActive(String blockId) {
        Block b = getBlock(blockId);
        b.activeCores += b.coresPerWorker;
        activeCores += b.coresPerWorker;
        requestedCores -= b.coresPerWorker;
    }
    
    public synchronized void workerLost(String blockId) {
        Block b = getBlock(blockId);
        b.activeCores -= b.coresPerWorker;
        activeCores -= b.coresPerWorker;
        failedCores += b.coresPerWorker;
    }

    public synchronized void workerShutDown(String blockId) {
        Block b = getBlock(blockId);
        b.activeCores -= b.coresPerWorker;
        activeCores -= b.coresPerWorker;
        doneCores += b.coresPerWorker;
    }
    
    private Block getBlock(String blockId) {
        Block b = blocks.get(blockId);
        if (b == null) {
            throw new IllegalArgumentException("Unknown block id: " + blockId);
        }
        return b;
    }

    public Map<String, Block> getBlocks() {
        return blocks;
    }

    public int getQueuedBlocks() {
        return queuedBlocks;
    }

    public int getActiveBlocks() {
        return activeBlocks;
    }

    public int getDoneBlocks() {
        return doneBlocks;
    }

    public int getFailedBlocks() {
        return failedBlocks;
    }

    public int getRequestedCores() {
        return requestedCores;
    }

    public int getActiveCores() {
        return activeCores;
    }

    public int getDoneCores() {
        return doneCores;
    }

    public int getFailedCores() {
        return failedCores;
    }
    
    private static Method getMethod(String name) {
        try {
            return CoasterStatusItem.class.getMethod(name, (Class<?>[]) null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public enum BlockState {
        QUEUED, ACTIVE, FAILED, DONE
    }
    
    public static class Block {
        public BlockState state;
        public final String id;
        public final int cores, coresPerWorker;
        public int activeCores;
        
        public Block(String id, int cores, int coresPerWorker) {
            this.id = id;
            this.cores = cores;
            this.coresPerWorker = coresPerWorker;
            this.state = BlockState.QUEUED;
        }
    }
}
