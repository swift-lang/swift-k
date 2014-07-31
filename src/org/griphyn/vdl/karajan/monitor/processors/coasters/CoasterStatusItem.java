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
    
    public synchronized void newBlock(String id, int cores, int coresPerWorker, int walltime) {
        Block b = new Block(id, cores, coresPerWorker, walltime);
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
    
    public synchronized void workerActive(String blockId, String workerId, String node, int cores, long now) {
        Block b = getBlock(blockId);
        activeCores += cores;
        requestedCores -= cores;
        b.addWorker(workerId, node, cores, now);
    }
    
    public synchronized void workerLost(String blockId, String workerId) {
        Block b = getBlock(blockId);
        Worker w = b.getWorker(workerId);
        int cores = w.cores;
        activeCores -= cores;
        failedCores += cores;
        b.activeCores -= cores;
        w.state = WorkerState.FAILED;
    }

    public synchronized void workerShutDown(String blockId, String workerId) {
        Block b = getBlock(blockId);
        Worker w = b.getWorker(workerId);
        int cores = w.cores;
        activeCores -= cores;
        failedCores += cores;
        b.activeCores -= cores;
        w.state = WorkerState.FAILED;
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
    
    public enum WorkerState {
        ACTIVE, FAILED, DONE
    }
    
    public static class Worker {
        public String node;
        public int cores;
        public WorkerState state;
        
        public Worker(String node, int cores) {
            this.node = node;
            this.cores = cores;
        }
    }
    
    public static class Block {
        public BlockState state;
        public final String id;
        public final int cores, coresPerWorker;
        public int activeCores;
        public int walltime;
        public long startTime;
        public Map<String, Worker> workers;
        
        public Block(String id, int cores, int coresPerWorker, int walltime) {
            this.id = id;
            this.cores = cores;
            this.coresPerWorker = coresPerWorker;
            this.state = BlockState.QUEUED;
            this.walltime = walltime;
        }

        public Worker getWorker(String workerId) {
            return workers.get(workerId);
        }

        public void addWorker(String workerId, String node, int cores, long now) {
            if (workers == null) {
                workers = new HashMap<String, Worker>();
            }
            Worker w = new Worker(node, cores);
            w.state = WorkerState.ACTIVE;
            workers.put(workerId, w);
            activeCores += cores;
            if (startTime == 0) {
                startTime = now;
            }
        }
    }
}
