// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 22, 2005
 */
package org.globus.cog.karajan.workflow.futures;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.NamedArgumentsListener;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;

public class FutureNamedArgument implements Future, NamedArgumentsListener {
	private boolean closed;
	private final NamedArguments named;
	private final String name;
	private Set<ListenerStackPair> listeners;
	private FutureEvaluationException exception;

	public FutureNamedArgument(String name, NamedArguments named) {
		this.name = name;
		this.named = named;
		named.addListener(name, this);
	}

	public void close() {
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}
	
	private Object notYetAvailable() {
		//there should be an abstract future
		if (exception != null) {
			throw exception;
		}
		else {
			throw new FutureNotYetAvailable(this);
		}
	}

	public synchronized Object getValue() {
		Object value = named.getArgument(name);
		if (value == null) {
			if (closed) {
				throw new KarajanRuntimeException(name);
			}
			else {
				return notYetAvailable();
			}
		}
		else {
			return value;
		}
	}

	public synchronized void addModificationAction(FutureListener target, VariableStack event) {
		if (listeners == null) {
			listeners = new HashSet();
		}
		listeners.add(new ListenerStackPair(target, event));
		/*
		 * Repeat after me: always check if the future was not closed
		 * between the time the fault was thrown and the time this
		 * method acquired the monitor
		 */
		if (isClosed()) {
			notifyListeners();
		}
	}

	public void namedArgumentAdded(String name, NamedArguments source) {
		if (name.equals(this.name)) {
			close();
			notifyListeners();
		}
	}

	private synchronized void notifyListeners() {
		if (listeners != null) {
			Iterator<ListenerStackPair> i = listeners.iterator();
			while (i.hasNext()) {
				ListenerStackPair etp = i.next();
				if (FuturesMonitor.debug) {
					FuturesMonitor.monitor.remove(etp);
				}
				i.remove();
				etp.listener.futureModified(this, etp.stack);
			}
		}
	}
	
	public void fail(FutureEvaluationException e) {
	    this.exception = e;
	    notifyListeners();
	}
}
