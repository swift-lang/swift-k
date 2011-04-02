// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.LoopNotificationEvent;

public class Break extends FlowNode {
	public void execute(VariableStack stack) throws ExecutionException {
		break_(stack);
	}

	protected void break_(VariableStack stack) throws ExecutionException {
		stack.leave();
		fireNotificationEvent(new LoopNotificationEvent(this, LoopNotificationEvent.BREAK, stack), stack);
	}
}