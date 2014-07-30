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
 * Created on Oct 14, 2011
 */
package org.globus.cog.abstraction.impl.file.coaster.buffers;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers.Direction;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.WriteIOCallback;
import org.globus.cog.coaster.channels.PerformanceDiagnosticInputStream;

public class ThrottleManager {
    public static final Logger logger = Logger.getLogger(ThrottleManager.class);
    
    private static final ThrottleManager IN = new ThrottleManager(Direction.IN);
    private static final ThrottleManager OUT = new ThrottleManager(Direction.OUT);
    
    public static final int MAX_CONCURRENT_TRANSFERS = 256;
    public static final double LAMBDA = 3;
    
    public static final long MIN_UPDATE_INTERVAL = 500; //ms
        
    /*
     * lambda = 0.2 gives a nice curve like this:
     * http://www.wolframalpha.com/input/?i=plot+256*exp%28-0.2*%28x%2F%28512-x%2B0.0001%29%29%29%2C+x+%3D+0+to+512
     */
    
    public static ThrottleManager getDefault(Direction dir) {
        switch (dir) {
            case IN: return IN;
            case OUT: return OUT;
            default: return null;
        }
    }
    
    private Stack<WriteIOCallback> active, suspended;
    private long lastTime;
    private int lastMaxTransfers;
    private Direction dir;
    
    public ThrottleManager(Direction dir) {
        this.dir = dir;
        active = new Stack<WriteIOCallback>();
        suspended = new Stack<WriteIOCallback>();
        lastTime = System.currentTimeMillis();
        lastMaxTransfers = MAX_CONCURRENT_TRANSFERS;
    }

    public void register(WriteIOCallback cb) {
        synchronized(this) {
            if (active.size() > lastMaxTransfers) {
                cb.suspend();
                // put this at the bottom of the stack, so earlier transfers
                // get priority
                suspended.insertElementAt(cb, 0);
            }
            else {
                active.push(cb);
            }
        }
    }
    
    public void unregister(WriteIOCallback cb) {
        synchronized(this) {
            if (!active.remove(cb)) {
                suspended.remove(cb);
            }
        }
    }
    
    public void update(int maxBuffers, int crtBuffers) {
        long now = System.currentTimeMillis();
        if (now - lastTime < MIN_UPDATE_INTERVAL) {
        	return;
        }
        lastTime = now;
        int allowed = allowedTransfers(maxBuffers, crtBuffers);
        log(allowed, maxBuffers, crtBuffers);
        // Use stacks because of the assumption that it's better to have
        // some transfers prioritized 
        synchronized(this) {
            while (active.size() > allowed) {
                suspendOne();
            }
            while (active.size() < allowed && !suspended.isEmpty()) {
                resumeOne();
            }
        }
    }
    
    private void log(int allowed, int maxBuffers, int crtBuffers) {
        if (logger.isDebugEnabled()) {
            logger.debug(dir + " maxBuffers=" + maxBuffers + ", crtBuffers=" + crtBuffers + 
                ", allowedTransfers=" + allowed + ", active=" + active.size() + 
                ", suspended=" + suspended.size());
            Runtime r = Runtime.getRuntime();
            if (dir == Direction.OUT) {
                logger.debug("mem=" + PerformanceDiagnosticInputStream.units(r.totalMemory() - r.freeMemory()) + 
                    "B, heap=" + PerformanceDiagnosticInputStream.units(r.totalMemory()) + 
                    "B, maxHeap=" + PerformanceDiagnosticInputStream.units(r.maxMemory()) + "B");
            }
        }
    }
    
    private void suspendOne() {
        WriteIOCallback h = active.pop();
        h.suspend();
        suspended.push(h);
    }
    
    private void resumeOne() {
        WriteIOCallback h = suspended.pop();
        h.resume();
        active.push(h);
    }

    private int allowedTransfers(int maxBuffers, int crtBuffers) {
        // 0 when crtBuffers = maxBuffers
        // MAX_CONCURRENT_TRANSFERS when crtBuffers = 0
        // some smooth function in between
        // the 0.0001 is there to approximate +inf when maxBuffers = crtBuffers.
        if (maxBuffers < crtBuffers) {
            return 0;
        }
        int allowed = (int) Math.round(MAX_CONCURRENT_TRANSFERS * 
            Math.exp(-LAMBDA * (crtBuffers/(maxBuffers - crtBuffers + 0.0001))));
        if (allowed < 0) {
        	allowed = 0;
        }
        return allowed;
    }
}
