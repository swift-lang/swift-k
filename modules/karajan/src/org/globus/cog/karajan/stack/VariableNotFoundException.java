
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 7, 2004
 */
package org.globus.cog.karajan.stack;

import org.globus.cog.karajan.workflow.ExecutionException;


public class VariableNotFoundException extends ExecutionException {
	private static final long serialVersionUID = -1796098713095927501L;

	public VariableNotFoundException() {
		super();
	}

	public VariableNotFoundException(String message) {
		super(message);
	}

	public VariableNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public VariableNotFoundException(Throwable cause) {
		super(cause);
	}

	public VariableNotFoundException(VariableStack stack, String message) {
		super(stack, message);
	}

	public VariableNotFoundException(VariableStack stack, String message, Throwable cause) {
		super(stack, message, cause);
	}
}