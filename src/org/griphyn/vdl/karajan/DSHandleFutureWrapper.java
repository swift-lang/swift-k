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
 * Created on Jun 8, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.LinkedList;
import java.util.List;

import k.rt.FutureListener;

import org.globus.cog.karajan.futures.FutureEvaluationException;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;

public class DSHandleFutureWrapper implements FutureWrapper {
    private LinkedList<FutureListener> listeners;
    private AbstractDataNode node;
    
    public DSHandleFutureWrapper(AbstractDataNode node) {
        this.node = node;
    }
    
    public void close() {
        node.closeShallow();
    }
    
    public boolean isClosed() {
        return node.isClosed();
    }

    public Object getValue() {
        Object v = node.getValue();
        if (v instanceof RuntimeException) {
            throw (RuntimeException) v;
        }
        return v;
    }

    public void fail(FutureEvaluationException e) {
        node.setValue(e);
    }
    
    @Override
    public void addListener(FutureListener l) {
        boolean closed;
        synchronized(this) {
            if (listeners == null) {
                listeners = new LinkedList<FutureListener>();
            }
            WaitingThreadsMonitor.addThread(l, node);
            listeners.add(l);
            closed = isClosed();
        }
        if (closed) {
            notifyListeners();
        }
    }
    
    public void notifyListeners() {
        List<FutureListener> ls;
        synchronized(this) {
            if (listeners == null) {
                return;
            }
            ls = listeners;
            listeners = null;
        }
        for (FutureListener l : ls) {
            WaitingThreadsMonitor.removeThread(l);
            l.futureUpdated(this);
        }
    }
    
	public int listenerCount() {
	    synchronized(node) {
    		if (listeners == null) {
    			return 0;
    		}
    		else {
    			return listeners.size();
    		}
	    }
	}
	
	@Override
    public synchronized List<FutureListener> getListeners() {
        return new LinkedList<FutureListener>(listeners);
    }

    public String toString() {
		return "F/" + node;
	}

    @Override
    public DSHandle getHandle() {
        return node;
    }
}
