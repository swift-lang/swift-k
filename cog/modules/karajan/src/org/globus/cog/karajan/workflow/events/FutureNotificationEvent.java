// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 16, 2005
 */
package org.globus.cog.karajan.workflow.events;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class FutureNotificationEvent extends NotificationEvent {
	public static final NotificationEventType FUTURE_MODIFIED = new NotificationEventType(
			"FUTURE_MODIFIED", 8);

	private final Future future;

	private final int subtype;

	public FutureNotificationEvent(int subtype, FlowElement source, Future f, VariableStack stack) {
		super(source, FUTURE_MODIFIED, stack);
		this.future = f;
		this.subtype = subtype;
	}

	public Future getFuture() {
		return future;
	}

	public int getSubtype() {
		return subtype;
	}
}
