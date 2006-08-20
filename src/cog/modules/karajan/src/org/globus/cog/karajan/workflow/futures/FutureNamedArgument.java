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
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.EventTargetPair;

public class FutureNamedArgument implements Future, NamedArgumentsListener {
	private boolean closed;
	private final NamedArguments named;
	private final String name;
	private Set listeners;

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

	public synchronized Object getValue() throws VariableNotFoundException {
		Object value = named.getArgument(name);
		if (value == null) {
			if (closed) {
				throw new VariableNotFoundException(name);
			}
			else {
				throw new FutureNotYetAvailable(this);
			}
		}
		else {
			return value;
		}
	}

	public synchronized void addModificationAction(EventListener target, Event event) {
		if (listeners == null) {
			listeners = new HashSet();
		}
		listeners.add(new EventTargetPair(event, target));
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
			Iterator i = listeners.iterator();
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
