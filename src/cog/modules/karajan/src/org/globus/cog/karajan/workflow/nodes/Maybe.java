//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 26, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;

public class Maybe extends SequentialChoice {
    
    protected void childCompleted(VariableStack stack) throws ExecutionException {
        startNext(stack);
    }
    
	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		if (NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
			complete(e.getStack());
		}
		else {
			super.notificationEvent(e);
		}
	}

	public void post(VariableStack stack) throws ExecutionException {
        commitBuffers(stack);
		complete(stack);
	}
}
