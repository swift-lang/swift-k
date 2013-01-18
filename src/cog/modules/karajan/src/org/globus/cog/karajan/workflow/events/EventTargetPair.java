// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Nov 3, 2003
 */
package org.globus.cog.karajan.workflow.events;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public final class EventTargetPair implements Runnable {
    public static final Logger logger = Logger.getLogger(EventTargetPair.class);
    
	private final FlowElement target;
	private final VariableStack event;

	public EventTargetPair(VariableStack event, FlowElement target) {
		this.target = target;
		this.event = event;
	}

	public VariableStack getEvent() {
		return event;
	}

	public FlowElement getTarget() {
		return target;
	}

	public String toString() {
		return event.toString() + " -> " + target.toString();
	}

	public void run() {
		EventBus.start(target, event);
	}
}
