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
 * Created on Jun 17, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import k.rt.FutureListener;
import k.thr.LWThread;

import org.griphyn.vdl.mapping.DSHandle;

public class WaitingThreadsMonitor {
	private static Map<LWThread, DSHandle> threads = new HashMap<LWThread, DSHandle>();
	private static Map<LWThread, List<DSHandle>> outputs = new HashMap<LWThread, List<DSHandle>>();;
	
	public static void addThread(FutureListener fl, DSHandle waitingOn) {
	    if (fl instanceof LWThread.Listener) {
	        addThread(((LWThread.Listener) fl).getThread(), waitingOn);
	    }
	}
	
	public static void removeThread(FutureListener fl) {
        if (fl instanceof LWThread.Listener) {
            removeThread(((LWThread.Listener) fl).getThread());
        }
    }
	
	public static void addThread(LWThread thr, DSHandle waitingOn) {
	    if (thr != null) {
	        synchronized(threads) {
	            threads.put(thr, waitingOn);
	        }
	    }
	}
		
	public static void removeThread(LWThread thr) {
	    synchronized(threads) {
	        threads.remove(thr);
	    }
	}
	
	public static Map<LWThread, DSHandle> getAllThreads() {
	    synchronized(threads) {
	        return new HashMap<LWThread, DSHandle>(threads);
	    }
	}

    public static void addOutput(LWThread thr, List<DSHandle> outputs) {
        synchronized(WaitingThreadsMonitor.outputs) {
            WaitingThreadsMonitor.outputs.put(thr, outputs);
        }
    }

    public static void removeOutput(LWThread thr) {
        synchronized(outputs) {
            outputs.remove(thr);
        }
    }
    
    public static Map<LWThread, List<DSHandle>> getOutputs() {
        synchronized(outputs) {
            return new HashMap<LWThread, List<DSHandle>>(outputs);
        }
    }
}
