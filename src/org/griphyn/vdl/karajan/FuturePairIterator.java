/*
 * Created on Jun 9, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.Iterator;
import java.util.LinkedList;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.ControlEvent;
import org.globus.cog.karajan.workflow.events.ControlEventType;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureIterator;
import org.globus.cog.karajan.workflow.futures.FutureIteratorIncomplete;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;

public class FuturePairIterator implements FutureIterator, EventListener, Mergeable {
	private ArrayIndexFutureList array;
	private int crt;
	private LinkedList listeners;

	public FuturePairIterator(ArrayIndexFutureList array) {
		this.array = array;
		array.addModificationAction(this, null);
	}

	public FuturePairIterator(ArrayIndexFutureList array, VariableStack stack) {
		this.array = array;
		array.addModificationAction(this, new ControlEvent(null, ControlEventType.RESTART, stack));
	}

	public synchronized boolean hasAvailable() {
		return crt < array.available();
	}

	public synchronized int current() {
		return crt;
	}

	public int count() {
		try {
			return array.size();
		}
		catch (FutureNotYetAvailable e) {
			throw new FutureIteratorIncomplete(array, this);
		}
	}

	public Object peek() {
		try {
			return array.get(crt);
		}
		catch (FutureNotYetAvailable e) {
			throw new FutureIteratorIncomplete(array, this);
		}
	}

	public void remove() {
		throw new UnsupportedOperationException("remove");
	}

	public synchronized boolean hasNext() {
		if (array.isClosed()) {
			return crt < array.size();
		}
		else {
			if (crt < array.available()) {
				return true;
			}
			else {
				throw new FutureIteratorIncomplete(array, this);
			}
		}
	}

	public synchronized Object next() {
		if (array.isClosed()) {
			return array.get(crt++);
		}
		else {
			if (crt < array.available()) {
				return array.get(crt++);
			}
			else {
				throw new FutureIteratorIncomplete(array, this);
			}
		}
	}

	public void close() {
		// nope
	}

	public boolean isClosed() {
		return array.isClosed();
	}

	public Object getValue() throws VariableNotFoundException {
		return this;
	}

	public synchronized void addModificationAction(EventListener target, Event event) {
		if (listeners == null) {
			listeners = new LinkedList();
		}
		WaitingThreadsMonitor.addThread(event.getStack());
		listeners.add(new EventTargetPair(event, target));
	}

	private void notifyListeners() {
		synchronized (this) {
			if (listeners == null) {
				return;
			}

			Iterator i = listeners.iterator();
			while (i.hasNext()) {
				EventTargetPair etp = (EventTargetPair) i.next();
				WaitingThreadsMonitor.removeThread(etp.getEvent().getStack());
				EventBus.post(etp.getTarget(), etp.getEvent());
				i.remove();
			}
			
			listeners = null;
		}
	}

	public void event(Event e) throws ExecutionException {
		notifyListeners();
	}

	public void mergeListeners(Future f) {
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			EventTargetPair etp = (EventTargetPair) i.next();
			f.addModificationAction(etp.getTarget(), etp.getEvent());
			i.remove();
		}
	}

	public void fail(FutureEvaluationException e) {
		//handled by the list
	}
}
