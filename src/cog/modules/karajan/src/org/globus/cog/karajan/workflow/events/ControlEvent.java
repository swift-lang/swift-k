//----------------------------------------------------------------------
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

public class ControlEvent extends FlowEvent {
	private ControlEventType type;
	
	public ControlEvent(FlowElement source, ControlEventType type, VariableStack stack) {
		super(EventClass.CONTROL_EVENT, source, stack);
		this.type = type;
	}
	
	public String toString() {
		return "ControlEvent:" + type;
	}
	
	public ControlEventType getType() {
		return type;
	}
	
	public void setType(ControlEventType type) {
		this.type = type;
	}
}
