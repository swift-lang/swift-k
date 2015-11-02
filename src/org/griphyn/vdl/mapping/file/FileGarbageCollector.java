/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.mapping.file;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.util.SwiftConfig;

public class FileGarbageCollector implements Runnable {
    public static final Logger logger = Logger.getLogger(FileGarbageCollector.class);
    
    private static FileGarbageCollector instance;
    
    public synchronized static FileGarbageCollector getDefault() {
        if (instance == null) {
            instance = new FileGarbageCollector();
        }
        return instance;
    }
    
    private LinkedList<RootHandle> queue;
    private Thread thread;
    private Set<RootHandle> nodes;
    private boolean shutdown, done, enabled;
    
    public FileGarbageCollector() {
        queue = new LinkedList<RootHandle>();
        nodes = Collections.synchronizedSet(new HashSet<RootHandle>());
        enabled = SwiftConfig.getDefault().isFileGCEnabled();
        if (enabled) {
            thread = new Thread(this, "File Garbage Collector");
            thread.setDaemon(true);
            thread.start();
        }
    }
        
    /**
     * Cleans the specified handle. Also removes it from the collector.
     */
    public void clean(RootHandle node) {
        if (!enabled) {
            return;
        }
        nodes.remove(node);
        queue(node);
    }
    
    private void queue(RootHandle node) {
        synchronized(queue) {
            queue.add(node);
            queue.notify();
        }
    }

    /**
     * Registers a root handle with the collector. The handle will 
     * be cleaned at some point in the future.
     */
    public void add(RootHandle node) {
        if (!enabled) {
            return;
        }
        nodes.add(node);
    }
    
    /**
     * Cleans all handles registered with this collector.
     */
    public void clean() {
        if (!enabled) {
            return;
        }
        synchronized(queue) {
            queue.addAll(nodes);
            nodes.clear();
            queue.notify();
        }
    }
    
    public void run() {
        try {
            while (true) {
                RootHandle node;
                synchronized(queue) {
                    while (queue.isEmpty() && !shutdown) {
                        queue.wait();
                    }
                    if (shutdown && queue.isEmpty()) {
                        done = true;
                        break;
                    }
                    node = queue.remove();
                }
                try {
                    AbstractDataNode dn = (AbstractDataNode) node;
                    if (!dn.isCleaned()) {
                        Mapper m = node.getMapper();
                        Collection<Path> fringe = node.getFringePaths();
                        for (Path p : fringe) {
                            PhysicalFormat pf = m.map(p);
                            if (!m.isPersistent(p)) {
                                pf.clean();
                                m.fileCleaned(pf);
                            }
                        }
                    }
                }
                catch (Exception e) {
                    logger.info("Failed to clean " + node, e);
                }
            }
        }
        catch (InterruptedException e) { 
        }
        done = true;
    }

    public void waitFor() throws InterruptedException {
        shutdown = true;
        while (!done && enabled) {
            synchronized(queue) {
                queue.notify();
            }
            Thread.sleep(1);
        }
    }
}
