// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 8, 2003
 */
package org.globus.cog.karajan.workflow;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.globus.cog.karajan.stack.Trace;
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
	
	public ExecutionException(VariableStack stack, Throwable cause) {
        this(stack, null, cause);
    }

	public ExecutionException(VariableStack stack, String message, Throwable cause) {
		this(message, cause);
		this.stack = stack.copy();
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

	public VariableStack getStack() {
		return stack;
	}

	public void setStack(VariableStack stack) {
		this.stack = stack.copy();
	}
	
	@Override
	public void printStackTrace() {
		StringBuffer sb = new StringBuffer();
		toString(sb, true, true);
		System.err.println(sb.toString());
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb, false, true);
		return sb.toString();
	}

	private void toString(StringBuffer sb, boolean trace, boolean first) {
		if (getMessage() != null || trace) {
			if (!first) {
				sb.append("Caused by: ");
			}
			sb.append(getMessage());
			sb.append('\n');
		}
		if (trace) {
			if (stack != null) {
				sb.append(Trace.get(stack));
				sb.append('\n');
			}
			else {
				//sb.append("\t-- no stack --\n");
			}
		}
		Throwable cause = getCause();
		if (cause != null) {
			if (cause instanceof ExecutionException) {
				((ExecutionException) cause).toString(sb, trace, false);
			}
			else {
				appendJavaException(sb, cause, trace);
			}
		}
	}

	private void appendJavaException(StringBuffer sb, Throwable cause, boolean trace) {
		if (cause instanceof RuntimeException && trace) {
			sb.append("Caused by: ");
			CharArrayWriter caw = new CharArrayWriter();
			cause.printStackTrace(new PrintWriter(caw));
			sb.append(caw.toString());
			sb.append('\n');
		}
		else {
			sb.append("Caused by: ");
			if (cause.getMessage() == null) {
				sb.append(cause.toString());
			}
			else {
				sb.append(cause.getMessage());
			}
			sb.append('\n');
		}
		if (cause.getCause() != null) {
			appendJavaException(sb, cause.getCause(), trace);
		}
	}
	
	//TODO
	public VariableStack getInitialStack() {
		return null;
	}
}
