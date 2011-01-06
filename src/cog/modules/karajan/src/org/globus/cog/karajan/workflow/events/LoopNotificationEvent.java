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

public class LoopNotificationEvent extends NotificationEvent {

	public static final NotificationEventType BREAK = new NotificationEventType("BREAK", 6);
	public static final NotificationEventType CONTINUE = new NotificationEventType("CONTINUE", 7);

	public LoopNotificationEvent(FlowElement flowElement, NotificationEventType eventType,
			VariableStack stack) {
		super(flowElement, eventType, stack);
	}

	public String toString() {
		return "Loop notification event: " + getType();
	}
}