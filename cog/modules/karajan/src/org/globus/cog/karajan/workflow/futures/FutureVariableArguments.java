// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2005
 */
package org.globus.cog.karajan.workflow.futures;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.arguments.VariableArgumentsImpl;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.EventTargetPair;

public class FutureVariableArguments extends VariableArgumentsImpl implements FutureList {
	private static final Logger logger = Logger.getLogger(FutureVariableArguments.class);

	private boolean closed;
	private List actions;
	private FutureEvaluationException exception;

	public FutureVariableArguments() {
		super();
	}

	public synchronized void append(Object value) {
		super.append(value);
		actions();
	}

	public synchronized void appendAll(List args) {
		super.appendAll(args);
		actions();
	}

	public void merge(VariableArguments args) {
		super.merge(args);
		actions();
	}

	public void close() {
		closed = true;
		modified();
		actions();
	}

	public boolean isClosed() {
		return closed;
	}

	public Object getValue() {
		return this;
	}

	public FutureIterator futureIterator() {
		return new Iterator(this, 0);
	}

	private Object notYetAvailable(String op)  {
		if (exception != null) {
			throw exception;
		}
		else {
			throw new FutureNotYetAvailable(this, op);
		}
	}

	private void checkClosed(String op) {
		if (!closed) {
			notYetAvailable(op);
		}
	}

	public VariableArguments copy() {
		checkClosed("copy()");
		return super.copy();
	}

	public Object get(int index) {
		if (!closed && (super.size() <= index)) {
			return notYetAvailable(null);
		}
		return super.get(index);
	}

	public List getAll() {
		checkClosed("getAll()");
		return super.getAll();
	}

	public java.util.Iterator iterator() {
		return new Iterator(this, 0);
	}

	public int size() {
		checkClosed("size()");
		return super.size();
	}

	public boolean isEmpty() {
		if (!super.isEmpty()) {
			return false;
		}
		else {
			checkClosed("isEmpty()");
			return true;
		}
	}

	public VariableArguments butFirst() {
		if (available() > 0) {
			removeFirst();
			return this;
		}
		else {
			return (VariableArguments) notYetAvailable(null);
		}
	}

	public Object[] toArray() {
		checkClosed("toArray()");
		return super.toArray();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("F[");
		for (int i = 0; i < super.size(); i++) {
			buf.append(super.get(i));
			buf.append(", ");
		}
		if (!closed) {
			buf.append("...");
		}
		buf.append(']');
		return buf.toString();
	}

	public synchronized void addModificationAction(EventListener target, Event event) {
		if (actions == null) {
			actions = new LinkedList();
		}
		EventTargetPair etp = new EventTargetPair(event, target);
		if (FuturesMonitor.debug) {
			FuturesMonitor.monitor.add(etp, this);
		}
		synchronized (actions) {
			actions.add(etp);
		}
		if (available() > 0 || closed) {
			actions();
		}
	}

	public List getModificationActions() {
		return actions;
	}

	private void actions() {
		if (actions != null) {
			synchronized (actions) {
				java.util.Iterator i = actions.iterator();
				while (i.hasNext()) {
					EventTargetPair etp = (EventTargetPair) i.next();
					if (FuturesMonitor.debug) {
						FuturesMonitor.monitor.remove(etp);
					}
					i.remove();
					EventBus.post(etp.getTarget(), etp.getEvent());
				}
			}
		}
	}

	public void fail(FutureEvaluationException e) {
		this.exception = e;
		actions();
	}

	public static class Iterator implements FutureIterator {
		private int crt;
		private final FutureVariableArguments vargs;

		public Iterator(FutureVariableArguments vargs, int crt) {
			if (vargs == null) {
				throw new RuntimeException("Unbound iterator");
			}
			this.vargs = vargs;
			this.crt = crt;
		}

		public boolean hasAvailable() {
			return vargs.available() > 0;
		}

		public void remove() {
			throw new UnsupportedOperationException("remove()");
		}

		public boolean hasNext() {
			if (vargs.closed) {
				return vargs.size() > 0;
			}
			else {
				if (hasAvailable()) {
					return true;
				}
				else {
					if (vargs.exception != null) {
						throw vargs.exception;
					}
					throw new FutureIteratorIncomplete(vargs, this);
				}
			}
		}

		public Object next() {
			synchronized (vargs) {
				Object obj = vargs.removeFirst();
				crt++;
				return obj;
			}
		}

		public Object peek() {
			synchronized (vargs) {
				return vargs.get(0);
			}
		}

		public void close() {
			vargs.close();
		}

		public boolean isClosed() {
			return vargs.isClosed();
		}

		public Object getValue() {
			return this;
		}

		public void addModificationAction(EventListener target, Event event) {
			vargs.addModificationAction(target, event);
		}

		public int current() {
			return crt;
		}

		public int count() {
			if (isClosed()) {
				return crt + vargs.size();
			}
			else {
				return -1;
			}
		}

		public FutureVariableArguments getVargs() {
			return vargs;
		}

		public void fail(FutureEvaluationException e) {
			// this would only be called on the backing channel
		}
	}

	public int available() {
		return super.size();
	}

	/**
	 * This should not normally be called. It's intended for serialization
	 */
	public List getBackingList() {
		return super.getAll();
	}
}