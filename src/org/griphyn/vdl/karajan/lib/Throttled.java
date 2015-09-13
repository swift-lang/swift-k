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


/*
 * Created on Oct 14, 2012
 */
package org.griphyn.vdl.karajan.lib;

import java.util.LinkedList;

import k.rt.ConditionalYield;
import k.rt.FutureObject;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.compiled.nodes.Sequential;
import org.griphyn.vdl.util.SwiftConfig;

public class Throttled extends Sequential {
    public static final int DEFAULT_MAX_THREADS = 1000000;
    
    private LinkedList<FutureObject> waiting;
    private int maxThreadCount, current;
    
    public Throttled() {
        maxThreadCount = (Integer) SwiftConfig.getDefault().getProperty("maxThreads", DEFAULT_MAX_THREADS);
        current = 0;
        waiting = new LinkedList<FutureObject>();
    }
    
    @Override
    public void run(LWThread thr) {
        int i = thr.checkSliceAndPopState(1);
        try {
            switch (i) {
                case 0:
                    tryIncCurrent();
                    i++;
                default:
                    super.run(thr);
                    decCurrent();
            }
        }
        catch (Yield y) {
            y.getState().push(i, 1);
            throw y;
        }
        catch (RuntimeException e) {
            decCurrent();
            throw e;
        }
    }
    
    private synchronized void decCurrent() {
        current--;
        if (!waiting.isEmpty()) {
            waiting.removeFirst().setValue(Boolean.TRUE);
        }
    }
    
    private synchronized void tryIncCurrent() {
        if (current == maxThreadCount) {
            FutureObject fo = new FutureObject();
            waiting.addLast(fo);
            throw new ConditionalYield(fo);
        }
        current++;
    }
}
