// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 *  
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;
import org.globus.cog.karajan.workflow.futures.ForwardArgumentFuture;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;

public class FutureNode extends SequentialWithArguments {
	private String name, channel;
	private Object value;
	private String[] names;

	static {
		setArguments(FutureNode.class, new Arg[] { Arg.VARGS });
	}

	public void executeChildren(VariableStack stack) throws ExecutionException {
		VariableStack copy = stack.copy();
		VariableArguments vargs = ArgUtil.getVariableArguments(stack);
		ret(stack, new ForwardArgumentFuture(vargs, 0));
		super.executeChildren(copy);
		complete(stack);
	}

	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		if (e.getType().equals(NotificationEventType.EXECUTION_FAILED)) {
			FutureVariableArguments fva = (FutureVariableArguments) ArgUtil.getVariableArguments(e.getStack()); 
			fva.fail(new FutureEvaluationException(((FailureNotificationEvent) e)));
		}
		else {
			super.notificationEvent(e);
		}
	}

	public void post(VariableStack stack) throws ExecutionException {
		FutureVariableArguments fva = (FutureVariableArguments) ArgUtil.getVariableArguments(stack);
		fva.close();
	}

	protected VariableArguments newVariableArguments() {
		return new FutureVariableArguments();
	}
}