/*
 * Created on Jun 9, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureIterator;
import org.globus.cog.karajan.workflow.futures.FutureList;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.globus.cog.util.CopyOnWriteHashSet;

public class ArrayIndexFutureList implements FutureList {
	private ArrayList keys;
	private Map values;
	private boolean closed;
	private CopyOnWriteHashSet listeners;
	private FutureEvaluationException exception;

	public ArrayIndexFutureList(Map values) {
		this.values = values;
		keys = new ArrayList();
	}
	
	private RuntimeException notYetAvailable() {
		if (exception != null) {
			return exception;
		}
		return new FutureNotYetAvailable(this);
	}

	public Object get(int index) {
		if (exception != null) {
			throw exception;
		}
		if (!closed && index >= keys.size()) {
			throw notYetAvailable();
		}
		else {
			Object key = keys.get(index);
			return new Pair(key, values.get(key));
		}
	}

	public int available() {
		return keys.size();
	}

	public void addKey(Object key) {
		keys.add(key);
		notifyListeners();
	}

	public FutureIterator futureIterator() {
		return new FuturePairIterator(this);
	}

	public FutureIterator futureIterator(VariableStack stack) {
		return new FuturePairIterator(this, stack);
	}

	public synchronized void close() {
		closed = true;
		Set allkeys = new HashSet(values.keySet());
		Iterator i = keys.iterator();
		while (i.hasNext()) {
			allkeys.remove(i.next());
		}
		// remaining keys must be added
		i = allkeys.iterator();
		while (i.hasNext()) {
			keys.add(i.next());
		}
		notifyListeners();
	}

	public boolean isClosed() {
		return closed;
	}

	public Object getValue() throws VariableNotFoundException {
		return this;
	}

	public synchronized void addModificationAction(EventListener target, Event event) {
		if (listeners == null) {
			listeners = new CopyOnWriteHashSet();
		}

		listeners.add(new EventTargetPair(event, target));
		if (closed) {
			notifyListeners();
		}
	}

	private synchronized void notifyListeners() {
		if (listeners == null) {
			return;
		}

		Iterator i = listeners.iterator();
		try {
			while (i.hasNext()) {
				try {
					EventTargetPair etp = (EventTargetPair) i.next();
					etp.getTarget().event(etp.getEvent());
				}
				catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		finally {
			listeners.release();
		}
	}

	public EventTargetPair[] getListenerEvents() {
		return (EventTargetPair[]) listeners.toArray(new EventTargetPair[0]);
	}

	public int size() {
		if (closed) {
			return keys.size();
		}
		else {
			throw notYetAvailable();
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
			return "Open, " + keys.size() + " elements, " + l;
		}
		else {
			return "Closed, " + keys.size() + " elements, " + l;
		}
	}
	
	public void fail(FutureEvaluationException e) {
		this.exception = e;
		notifyListeners();
	}
	
	public FutureEvaluationException getException() {
		return exception;
	}
}
