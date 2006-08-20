// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.events;

import java.util.Iterator;

import org.globus.cog.karajan.stack.Trace;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class ChainedFailureNotificationEvent extends FailureNotificationEvent {
	private NotificationEvent prev;

	public ChainedFailureNotificationEvent(FlowElement flowElement, FailureNotificationEvent e) {
		super(flowElement, e.getStack(), e.getInitialStack(), e.getMessage(), e.getException());
		prev = e;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getInitial().toString());
		VariableStack stack = getInitialStack();
		if (stack != null) {
			Iterator i = stack.getAllVars(Trace.ELEMENT).iterator();
			while (i.hasNext()) {
				sb.append("\n\t");
				sb.append(i.next().toString());
			}
		}
		return sb.toString();
	}

	public NotificationEvent getInitial() {
		if (prev instanceof ChainedFailureNotificationEvent) {
			return ((ChainedFailureNotificationEvent) prev).getInitial();
		}
		else {
			return prev;
		}
	}

	public void setPrevious(NotificationEvent previous) {
		this.prev = previous;
	}
}