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

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.griphyn.vdl.mapping.DSHandle;

public class WaitingThreadsMonitor {
    private static class StackTCPair {
        public final VariableStack stack;
        public final ThreadingContext tc;
        
        public StackTCPair(VariableStack stack) {
            this.stack = stack;
            try {
                this.tc = ThreadingContext.get(stack);
            }
            catch (VariableNotFoundException e) {
                throw new RuntimeException("Cannot get thread id", e);
            }
        }

        @Override
        public int hashCode() {
            return tc.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof StackTCPair) {
                return ((StackTCPair) obj).tc == tc;
            }
            else {
                return false;
            }
        }

        @Override
        public String toString() {
            return tc.toString();
        }
    }
    
	private static Map<StackTCPair, DSHandle> threads = new HashMap<StackTCPair, DSHandle>();
	private static Map<StackTCPair, List<DSHandle>> outputs = new HashMap<StackTCPair, List<DSHandle>>();;
	
	public static void addThread(VariableStack stack, DSHandle waitingOn) {
	    if (stack != null) {
	        synchronized(threads) {
	            threads.put(new StackTCPair(stack), waitingOn);
	        }
	    }
	}
		
	public static void removeThread(VariableStack stack) {
	    if (stack != null) {
	        synchronized(threads) {
	            threads.remove(new StackTCPair(stack));
	        }
	    }
	}
	
	public static Map<VariableStack, DSHandle> getAllThreads() {
	    synchronized(threads) {
	        Map<VariableStack, DSHandle> m = new HashMap<VariableStack, DSHandle>();
	        for (Map.Entry<StackTCPair, DSHandle> e : threads.entrySet()) {
	            m.put(e.getKey().stack, e.getValue());
	        }
	        return m;
	    }
	}

    public static void addOutput(VariableStack stack, List<DSHandle> outputs) {
        synchronized(WaitingThreadsMonitor.outputs) {
            WaitingThreadsMonitor.outputs.put(new StackTCPair(stack), outputs);
        }
    }

    public static void removeOutput(VariableStack stack) {
        synchronized(outputs) {
            outputs.remove(new StackTCPair(stack));
        }
    }
    
    public static Map<VariableStack, List<DSHandle>> getOutputs() {
        synchronized(outputs) {
            Map<VariableStack, List<DSHandle>> m = new HashMap<VariableStack, List<DSHandle>>();
            for (Map.Entry<StackTCPair, List<DSHandle>> e : outputs.entrySet()) {
                m.put(e.getKey().stack, e.getValue());
            }
            return m;
        }
    }
}
