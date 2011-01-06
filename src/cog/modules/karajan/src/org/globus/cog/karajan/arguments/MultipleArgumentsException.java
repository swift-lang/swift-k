// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 14, 2005
 */
package org.globus.cog.karajan.arguments;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class MultipleArgumentsException extends ExecutionException {
	private static final long serialVersionUID = -6495277993748613947L;

	public MultipleArgumentsException() {
		super();

	}

	public MultipleArgumentsException(String message) {
		super(message);

	}

	public MultipleArgumentsException(String message, Throwable cause) {
		super(message, cause);

	}

	public MultipleArgumentsException(Throwable cause) {
		super(cause);

	}

	public MultipleArgumentsException(VariableStack stack, String message) {
		super(stack, message);

	}

	public MultipleArgumentsException(VariableStack stack, String message, Throwable cause) {
		super(stack, message, cause);

	}
}