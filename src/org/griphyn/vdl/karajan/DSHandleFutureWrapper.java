/*
 * Created on Jun 8, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.Iterator;
import java.util.LinkedList;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DSHandleListener;

public class DSHandleFutureWrapper implements Future, DSHandleListener {
	private DSHandle handle;
	private LinkedList listeners;

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

	public synchronized Object getValue() throws VariableNotFoundException {
		Object value = handle.getValue();
		if (value instanceof RuntimeException) {
			throw (RuntimeException) value;
		}
		else {
			return value;
		}
	}

	public synchronized void addModificationAction(EventListener target, Event event) {
		/**
		 * So, the strategy is the following: getValue() or something else
		 * throws a future exception; then some entity catches that and calls
		 * this method. There is no way to ensure that the future was not closed
		 * in the mean time. What has to be done is that this method should
		 * check if the future was closed or modified at the time of the call of
		 * this method and call notifyListeners().
		 */
		if (listeners == null) {
			listeners = new LinkedList();
		}
		listeners.add(new EventTargetPair(event, target));
		WaitingThreadsMonitor.addThread(event.getStack());
		if (handle.isClosed()) {
			notifyListeners();
		}
	}

	private synchronized void notifyListeners() {
		if (listeners == null) {
			return;
		}
		while (!listeners.isEmpty()) {
			EventTargetPair etp = (EventTargetPair) listeners.removeFirst();
			WaitingThreadsMonitor.removeThread(etp.getEvent().getStack());
			EventBus.post(etp.getTarget(), etp.getEvent());
		}
		listeners = null;
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
		return "F/" + handle;
	}

	public void fail(FutureEvaluationException e) {
		handle.setValue(e);
		handle.closeShallow();
	}

	public void handleClosed(DSHandle handle) {
		notifyListeners();
	}
}
