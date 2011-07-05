//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 4, 2011
 */
package org.griphyn.vdl.mapping.file;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.griphyn.vdl.mapping.PhysicalFormat;

public class FileGarbageCollector implements Runnable {
    public static final Logger logger = Logger.getLogger(FileGarbageCollector.class);
    
    private static FileGarbageCollector instance;
    
    public synchronized static FileGarbageCollector getDefault() {
        if (instance == null) {
            instance = new FileGarbageCollector();
        }
        return instance;
    }
    
    private Queue<PhysicalFormat> queue;
    private Thread thread;
    private Map<PhysicalFormat, Integer> usageCount;
    private Set<PhysicalFormat> persistent;
    private boolean shutdown, done;
    
    public FileGarbageCollector() {
        queue = new LinkedList<PhysicalFormat>();
        usageCount = new HashMap<PhysicalFormat, Integer>();
        persistent = new HashSet<PhysicalFormat>();
        thread = new Thread(this, "File Garbage Collector");
        thread.setDaemon(true);
        thread.start();
    }
    
    public synchronized void markAsPersistent(PhysicalFormat pf) {
        persistent.add(pf);
    }
    
    public synchronized void decreaseUsageCount(PhysicalFormat pf) {
        assert Thread.holdsLock(this);
        Integer c = usageCount.get(pf);
        if (c == null) {
            if (persistent.remove(pf)) {
                logger.debug("Usage count of " + pf + " is 0, but file is persistent. Not removing.");
            }
            else {
                logger.debug("Usage count of " + pf + " is 0. Queueing for removal.");
                queue.add(pf);
                this.notify();
            }
        }
        else {
            if (c == 2) {
                usageCount.remove(pf);
                logger.debug("Decreasing usage count of " + pf + " to 1");
            }
            else {
                usageCount.put(pf, c - 1);
                logger.debug("Decreasing usage count of " + pf + " to " + c);
            }
        }
    }
    
    public synchronized void increaseUsageCount(PhysicalFormat pf) {
        // a usage count of 1 is assumed if the key is not in the map
        // A remap of a remappable mapper would increase the usage count to 2 
        Integer c = usageCount.get(pf);
        if (c == null) {
            usageCount.put(pf, 2);
            logger.debug("Increasing usage count of " + pf + " to 2");
        }
        else {
            usageCount.put(pf, c + 1);
            logger.debug("Increasing usage count of " + pf + " to " + (c + 1));
        }
    }
    
    public void run() {
        try {
            while (true) {
                PhysicalFormat pf;
                synchronized(this) {
                    while (queue.isEmpty() && !shutdown) {
                        this.wait();
                    }
                    if (shutdown) {
                        done = true;
                        break;
                    }
                    pf = queue.remove();
                }
                try {
                    pf.clean();
                }
                catch (Exception e) {
                    logger.info("Failed to clean " + pf, e);
                }
            }
        }
        catch (InterruptedException e) {
        }
    }

    public void waitFor() throws InterruptedException {
        shutdown = true;
        while (!done) {
            synchronized(this) {
                notify();
            }
            Thread.sleep(1);
        }
    }
}
