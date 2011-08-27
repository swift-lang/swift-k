/*
 * Created on Jun 8, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.futures.ListenerStackPair;
import org.griphyn.vdl.mapping.AbstractDataNode;

public class DSHandleFutureWrapper implements FutureWrapper {
    private LinkedList<ListenerStackPair> listeners;
    private AbstractDataNode node;
    
    public DSHandleFutureWrapper(AbstractDataNode node) {
        this.node = node;
    }
    
    public void addModificationAction(FutureListener target, VariableStack stack) {
        /**
         * TODO So, the strategy is the following: getValue() or something else
         * throws a future exception; then some entity catches that and calls
         * this method. There is no way to ensure that the future was not closed
         * in the mean time. What has to be done is that this method should
         * check if the future was closed or modified at the time of the call of
         * this method and call notifyListeners().
         */
        synchronized(node) {
            if (listeners == null) {
                listeners = new LinkedList<ListenerStackPair>();
            }
            listeners.add(new ListenerStackPair(target, stack));
            WaitingThreadsMonitor.addThread(stack);
            if (!node.isClosed()) {
                return;
            }
        }
        // closed == true;
        notifyListeners();
    }

    public void notifyListeners() {
        List<ListenerStackPair> l;
        synchronized(node) {
            if (listeners == null) {
                return;
            }
            
            l = listeners;
            listeners = null;
        }
        
        for (final ListenerStackPair lsp : l) {
            WaitingThreadsMonitor.removeThread(lsp.stack);
            EventBus.post(new Runnable() {
                public void run() {
                    lsp.listener.futureModified(DSHandleFutureWrapper.this, lsp.stack);
                } 
            });
        }
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
	
	private static final EventTargetPair[] EVENT_ARRAY = new EventTargetPair[0];

	public EventTargetPair[] getListenerEvents() {
	    synchronized(node) {
    		if (listeners != null) {
    			return listeners.toArray(EVENT_ARRAY);
    		}
    		else {
    			return null;
    		}
	    }
	}

	public String toString() {
		return "F/" + node;
	}
}
