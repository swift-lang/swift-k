//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 8, 2005
 */
package org.globus.cog.karajan.workflow.events;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.AbortException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class AbortNotificationEvent extends FailureNotificationEvent {
    
    public AbortNotificationEvent(FlowElement source, VariableStack stack) {
        this(source, stack, null);
    }
    
	public AbortNotificationEvent(FlowElement source, VariableStack stack, String message) { 
		super(source, stack, "Aborted", new AbortException(stack, message));
	}
}
