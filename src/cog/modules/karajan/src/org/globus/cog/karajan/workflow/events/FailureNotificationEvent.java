// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 14, 2005
 */
package org.globus.cog.karajan.workflow.events;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class FailureNotificationEvent extends NotificationEvent {
	private final String message;
	private final ExecutionException exception;
	private final VariableStack initialStack;

	public FailureNotificationEvent(FlowElement source, VariableStack stack,
			VariableStack initialStack, String message, Throwable exception) {
		super(source, NotificationEventType.EXECUTION_FAILED, stack);
		this.initialStack = initialStack;
		if (exception instanceof NullPointerException) {
			this.message = "NullPointerException";
		}
		else {
			this.message = message;
		}
		if (exception instanceof ExecutionException) {
			this.exception = (ExecutionException) exception;
		}
		else {
		    this.exception = new ExecutionException(stack, this.message, exception);
		}
	}

	public FailureNotificationEvent(FlowElement source, VariableStack stack, String message,
			Throwable exception) {
		this(source, stack, stack != null ? stack.copy() : null, message, exception);
	}

	public String getMessage() {
		return message;
	}

	public ExecutionException getException() {
		return exception;
	}

	public VariableStack getInitialStack() {
		return initialStack;
	}

	public String toString() {
		if (message == null) {
			if (exception == null) {
				return "NotificationEvent: EXECUTION_FAILED " + "\n\t" + getFlowElement();
			}
			else {
				return ExecutionContext.getMeaningfulMessage(exception) + "\n\t"
						+ getFlowElement();
			}
		}
		else {
			return message + "\n\t" + getFlowElement();
		}
	}

}
