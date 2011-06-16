/*
 * Created on Jun 8, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.futures.ListenerStackPair;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DSHandleListener;

public class DSHandleFutureWrapper implements Future, DSHandleListener {
	private DSHandle handle;
	private LinkedList<ListenerStackPair> listeners;

	public DSHandleFutureWrapper(DSHandle handle) {
		this.handle = handle;
		handle.addListener(this);
	}

	public synchronized void close() {
		handle.closeShallow();
	}

	public synchronized boolean isClosed() {
		return handle.isClosed();
	}

	public synchronized Object getValue() {
		Object value = handle.getValue();
		if (value instanceof RuntimeException) {
			throw (RuntimeException) value;
		}
		else {
			return value;
		}
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
	    synchronized(this) {
    		if (listeners == null) {
    			listeners = new LinkedList<ListenerStackPair>();
    		}
    		listeners.add(new ListenerStackPair(target, stack));
    		WaitingThreadsMonitor.addThread(stack);
    		if (!handle.isClosed()) {
    		    return;
    		}
	    }
	    // handle.isClosed();
		notifyListeners();
	}

	private void notifyListeners() {
	    List<ListenerStackPair> l;
	    synchronized(this) {
	        if (listeners == null) {
	            return;
	        }
	        
	        l = listeners;
	        listeners = null;
	    }
	    
	    for (ListenerStackPair lsp : l) {
			WaitingThreadsMonitor.removeThread(lsp.stack);
			lsp.listener.futureModified(DSHandleFutureWrapper.this, lsp.stack);
		}
	}

	public synchronized int listenerCount() {
		if (listeners == null) {
			return 0;
		}
		else {
			return listeners.size();
		}
	}

	public synchronized EventTargetPair[] getListenerEvents() {
		if (listeners != null) {
			return (EventTargetPair[]) listeners.toArray(new EventTargetPair[0]);
		}
		else {
			return null;
		}
	}

	public String toString() {
		String l;
        if (listeners == null) {
            l = "no listeners";
        }
        else {
            l = listeners.size() + " listeners";
        }
        if (!isClosed()) {
            return "Open, " + l;
        }
        else {
            return "Closed, " + l;
        }
	}

	public void fail(FutureEvaluationException e) {
		handle.setValue(e);
		handle.closeShallow();
	}

	public void handleClosed(DSHandle handle) {
		notifyListeners();
	}
}
