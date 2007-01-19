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

	public ExecutionException(VariableStack stack, String message, Throwable cause) {
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

	public VariableStack getStack() {
		return stack;
	}

	public void setStack(VariableStack stack) {
		this.stack = stack;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}

	private void toString(StringBuffer sb) {
		sb.append(getMessage());
		sb.append('\n');
		if (stack != null) {
			sb.append(Trace.get(stack));

		}
		else {
			//sb.append("\t-- no stack --\n");
		}
		Throwable cause = getCause();
		if (cause != null) {
			sb.append("Caused by: ");
			if (cause instanceof ExecutionException) {
				((ExecutionException) cause).toString(sb);
			}
			else {
				appendJavaException(sb, cause);
			}
		}
	}

	private void appendJavaException(StringBuffer sb, Throwable cause) {
		if (cause instanceof RuntimeException) {
			CharArrayWriter caw = new CharArrayWriter();
			cause.printStackTrace(new PrintWriter(caw));
			sb.append(caw.toString());
		}
		else {
			sb.append(cause.toString());
		}
		if (cause.getCause() != null) {
			sb.append("\nCaused by: ");
			appendJavaException(sb, cause.getCause());
		}
	}
}
