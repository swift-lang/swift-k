// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 22, 2004
 */
package org.globus.cog.karajan.workflow.events;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public abstract class Event {
	private final Priority priority;

	private FlowElement flowElement;
	
	private final EventClass cls;

	public Event(EventClass cls, FlowElement flowElement, Priority priority) {
		this.cls = cls;
		this.flowElement = flowElement;
		this.priority = priority;
	}
	
	public FlowElement getFlowElement() {
		return flowElement;
	}

	public void setFlowElement(FlowElement element) {
		flowElement = element;
	}

	public final Priority getPriority() {
		return priority;
	}
	
	public boolean hasStack() {
		return false;
	}
	
	public VariableStack getStack() {
		return null;
	}
		
	public final EventClass getEventClass() {
		return cls;
	}
}