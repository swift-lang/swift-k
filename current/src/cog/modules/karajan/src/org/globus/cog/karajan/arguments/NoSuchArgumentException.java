
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 7, 2004
 */
package org.globus.cog.karajan.arguments;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;


public class NoSuchArgumentException extends ExecutionException {
	private static final long serialVersionUID = 9192868467523879149L;

	public NoSuchArgumentException() {
		super();
	}

	public NoSuchArgumentException(String message) {
		super(message);
	}

	public NoSuchArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchArgumentException(Throwable cause) {
		super(cause);
	}

	public NoSuchArgumentException(VariableStack stack, String message) {
		super(stack, message);
	}

	public NoSuchArgumentException(VariableStack stack, String message, Throwable cause) {
		super(stack, message, cause);
	}
}
