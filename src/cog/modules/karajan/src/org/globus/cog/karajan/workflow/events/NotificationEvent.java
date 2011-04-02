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

public class NotificationEvent extends FlowEvent {

	private NotificationEventType type;

	public NotificationEvent(FlowElement source, NotificationEventType type, VariableStack stack) {
		super(EventClass.NOTIFICATION_EVENT, source, stack, Priority.HIGH);
		this.type = type;
	}

	public NotificationEventType getType() {
		return type;
	}

	public void setType(NotificationEventType type) {
		this.type = type;
	}
	
	public String toString() {
		return "NotificationEvent:"+getType();
	}
}