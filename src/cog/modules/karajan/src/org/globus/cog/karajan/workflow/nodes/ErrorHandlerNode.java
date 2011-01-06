// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 16, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.Trace;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ErrorHandler;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;

public class ErrorHandlerNode extends PartialArgumentsContainer {
	public static final String ERROR_HANDLERS = "#errorhandlers";

	public static final Arg A_MATCH = new Arg.Positional("match", 0);

	static {
		setArguments(ErrorHandlerNode.class, new Arg[] { A_MATCH });
	}

	public ErrorHandlerNode() {
		setOptimize(false);
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		String type = TypeUtil.toString(A_MATCH.getValue(stack));
		List handlers;
		if (stack.parentFrame().isDefined(ERROR_HANDLERS)) {
			handlers = TypeUtil.toList(stack.currentFrame().getVar(ERROR_HANDLERS));
		}
		else {
			handlers = new LinkedList();
			stack.parentFrame().setVar(ERROR_HANDLERS, handlers);
		}
		handlers.add(new ErrorHandler(type, this));
		super.partialArgumentsEvaluated(stack);
		post(stack);
	}

	public void handleError(FlowElement source, FailureNotificationEvent error)
			throws ExecutionException {
		VariableStack stack = error.getInitialStack();
		if (stack.isDefined("#inhandler") && !stack.currentFrame().isDefined("#inhandler")) {
			this.failImmediately(stack, error);
			return;
		}
		stack.enter();
		stack.setVar(Trace.ELEMENT, this);
		stack.setVar("element", source);
		stack.setVar("error", error.getMessage());
		stack.setVar("trace", error.toString());
		setArgsDone(stack);
		if (error.getException() != null) {
			stack.setVar("exception", error.getException());
		}
		else {
			stack.setVar("exception", "No exception available");
		}
		int errorcount = 1;
		stack.currentFrame().setBooleanVar("#inhandler", true);
		startRest(stack);
	}

	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		VariableStack stack = e.getStack();
		if (NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
			failImmediately(stack, (FailureNotificationEvent) e);
		}
		else {
			super.notificationEvent(e);
		}
	}
}