//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 8, 2005
 */
package org.globus.cog.karajan.workflow;

import org.globus.cog.karajan.stack.VariableStack;

public class AbortException extends ExecutionException {
	public AbortException() {
		super();
	}
	
	public AbortException(VariableStack stack) {
		this(stack, null);
	}

	public AbortException(VariableStack stack, String message) {
		super(stack, message == null ? "Abort" : message);
	}
}
