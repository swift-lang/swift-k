// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 22, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureNameBindingVariableArguments;
import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;

public class FutureIteratorNode extends PartialArgumentsContainer {
	public static final String FUTURE_ITERATOR = "##iterator";

	static {
		setArguments(FutureIteratorNode.class, new Arg[] { Arg.VARGS });
	}

	public FutureIteratorNode() {
		setQuotedArgs(true);
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		VariableArguments vargs = ArgUtil.getVariableArguments(stack);
		ret(stack, vargs);
		super.partialArgumentsEvaluated(stack);
		VariableStack copy = stack.copy();
		ArgUtil.setVariableArguments(stack, vargs);
		startRest(copy);
		complete(stack);
	}

	protected VariableArguments newVariableArguments() {
		return new FutureVariableArguments();
	}

	protected VariableArguments newNameBindingVariableArguments(NamedArguments nargs,
			List nonpropargs) {
		return new FutureNameBindingVariableArguments(nargs, nonpropargs);
	}

	public void post(VariableStack stack) throws ExecutionException {
		FutureVariableArguments fva = (FutureVariableArguments) ArgUtil.getVariableArguments(stack);
		fva.close();
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
}
