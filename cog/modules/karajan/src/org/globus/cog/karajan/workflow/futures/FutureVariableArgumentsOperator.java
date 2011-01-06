// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2005
 */
package org.globus.cog.karajan.workflow.futures;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.arguments.VariableArgumentsListener;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.EventTargetPair;

public abstract class FutureVariableArgumentsOperator implements VariableArguments, Future {
	private boolean closed;
	private List actions;
	private Object value;
	
	public FutureVariableArgumentsOperator(Object initialValue) {
		value = initialValue;
	}

	public void close() {
		closed = true;
		actions();
	}

	public boolean isClosed() {
		return closed;
	}

	public Object getValue() throws FutureNotYetAvailable {
		if (!closed) {
			return this;
		}
		else {
			return value;
		}
	}

	private void checkClosed(String op) {
		if (!closed) {
			throw new FutureNotYetAvailable(this, op);
		}
	}

	public String toString() {
		if (!closed) {
			StringBuffer buf = new StringBuffer();
			buf.append('<');
			if (value != null) {
				buf.append(value);
			}
			buf.append("?>");
			return buf.toString();
		}
		else {
			return value.toString();
		}
	}

	public synchronized void addModificationAction(EventListener target, Event event) {
		if (actions == null) {
			actions = new LinkedList();
		}
		synchronized (actions) {
			actions.add(new EventTargetPair(event, target));
		}
	}

	private void actions() {
		if (actions != null) {
			synchronized (actions) {
				java.util.Iterator i = actions.iterator();
				while (i.hasNext()) {
					EventTargetPair etp = (EventTargetPair) i.next();
					i.remove();
					EventBus.post(etp.getTarget(), etp.getEvent());
				}
			}
		}
	}

	public int available() {
		if (closed) {
			return  1;
		}
		else {
			return 0;
		}
	}
	
	
	public synchronized void append(Object o) {
		value = update(value, o);
	}
	
	public synchronized void appendAll(List args) {
		value = update(value, args);
	}
	
	public void merge(VariableArguments args) {
		value = update(value, args.getAll());
	}
	
	public final Object initialValue() {
		return null;
	}
	
	public abstract Object update(Object old, Object value);
	
	public Object update(Object old, List values) {
		Iterator i = values.iterator();
		Object tmp = value;
		while (i.hasNext()) {
			tmp = update(tmp, i.next());
		}
		return tmp;
	}

	public List getAll() {
		throw new UnsupportedOperationException();
	}

	public void set(List vargs) {
		throw new UnsupportedOperationException();
	}

	public Object get(int index) {
		throw new UnsupportedOperationException();
	}

	public VariableArguments copy() {
		throw new UnsupportedOperationException();
	}

	public int size() {
		throw new UnsupportedOperationException();
	}

	public Iterator iterator() {
		throw new UnsupportedOperationException();
	}

	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	public void set(VariableArguments other) {
		throw new UnsupportedOperationException();
	}

	public Object removeFirst() {
		throw new UnsupportedOperationException();
	}

	public void addListener(VariableArgumentsListener l) {
		throw new UnsupportedOperationException();
	}

	public void removeListener(VariableArgumentsListener l) {
		throw new UnsupportedOperationException();
	}
}