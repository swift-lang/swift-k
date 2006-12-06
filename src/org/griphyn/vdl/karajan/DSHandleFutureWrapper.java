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

public class DSHandleFutureWrapper implements Future, Mergeable {
	private DSHandle handle;
	private boolean closed;
	private LinkedList listeners;
	private FutureEvaluationException exception;

	public DSHandleFutureWrapper(DSHandle handle) {
		this.handle = handle;
	}

	public synchronized void close() {
		closed = true;
		notifyListeners();
	}

	public synchronized boolean isClosed() {
		return closed;
	}

	public synchronized Object getValue() throws VariableNotFoundException {
		if (exception != null) {
			throw exception;
		}
		return handle.getValue();
	}

	public synchronized void addModificationAction(EventListener target, Event event) {
		/**
		 * So, the strategy is the following:
		 *  getValue() or something else throws a future exception; then
		 *  some entity catches that and calls this method.
		 *  There is no way to ensure that the future was not closed in the
		 *  mean time. What has to be done is that this method should check
		 *  if the future was closed or modified at the time of the call of 
		 *  this method and call notifyListeners().
		 */
		if (listeners == null) {
			listeners = new LinkedList();
		}
		listeners.add(new EventTargetPair(event, target));
		WaitingThreadsMonitor.addThread(event.getStack());
		if (closed || handle.getValue() != null) {
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
	}

	public void mergeListeners(Future f) {
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			EventTargetPair etp = (EventTargetPair) i.next();
			f.addModificationAction(etp.getTarget(), etp.getEvent());
			i.remove();
		}
	}

	public EventTargetPair[] getListenerEvents() {
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
		if (!closed) {
			return "Open, " + l;
		}
		else {
			return "Closed, " + l;
		}
	}

	public void fail(FutureEvaluationException e) {
		this.exception = e;
		notifyListeners();
	}
}
