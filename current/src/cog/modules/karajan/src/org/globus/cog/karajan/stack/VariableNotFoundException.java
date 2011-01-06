
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

	public VariableNotFoundException(String name) {
		super(name);
	}

	public VariableNotFoundException(String name, Throwable cause) {
		super(name, cause);
	}

	public VariableNotFoundException(Throwable cause) {
		super(cause);
	}

	public VariableNotFoundException(VariableStack stack, String name) {
		super(stack, name);
	}

	public VariableNotFoundException(VariableStack stack, String name, Throwable cause) {
		super(stack, name, cause);
	}
	
	public String getMessage() {
		return "Variable not found: " + super.getMessage();
	}
}