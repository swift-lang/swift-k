// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 11, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class IgnoreErrorsNode extends AbstractRegexpFailureHandler {
	public static final Arg A_MATCH = new Arg.Optional("match");

	private static final String MATCH = "##match";

	static {
		setArguments(IgnoreErrorsNode.class, new Arg[] { A_MATCH });
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		if (A_MATCH.isPresent(stack)) {
			stack.setVar(MATCH, A_MATCH.getValue(stack));
		}
		super.partialArgumentsEvaluated(stack);
		startRest(stack);
	}

	public void failed(VariableStack stack, ExecutionException e) throws ExecutionException {
		if (!stack.currentFrame().isDefined(MATCH)) {
			super.completed(stack);
			return;
		}
		else {
			String match = (String) stack.currentFrame().getVar(MATCH);
			if (matches(match, e)) {
				super.completed(stack);
				return;
			}
		}
		super.failed(stack, e);
	}
}
