
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 8, 2003
 */
package org.globus.cog.karajan.workflow;

import org.globus.cog.karajan.stack.VariableStack;

public class ExecutionException extends Exception {
	private static final long serialVersionUID = 4975303013364072936L;
	
	private VariableStack stack;
	
	public ExecutionException() {
		super();
	}
	
	public ExecutionException(VariableStack stack, String message) {
		this(stack, message, null);
	}

	public ExecutionException(VariableStack stack, String message, Throwable cause){
		this(message, cause);
		this.stack = stack;
	}
	
	public ExecutionException(String message) {
		super(message);
	}

	public ExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ExecutionException(Throwable cause) {
		super(cause);
	}
	
	public VariableStack getStack(){
		return stack;
	}
}
