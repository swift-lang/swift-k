// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 25, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.FlowEvent;

public class GenerateErrorNode extends SequentialWithArguments {
	
	public static final Arg A_MESSAGE = new Arg.Positional("message", 0);
	public static final Arg A_EXCEPTION = new Arg.Positional("exception", 1);

	static {
		setArguments(GenerateErrorNode.class, new Arg[] { A_MESSAGE, A_EXCEPTION });
	}

	public void post(VariableStack stack) throws ExecutionException {
		if (A_EXCEPTION.isPresent(stack)) {
			Object exception = A_EXCEPTION.getValue(stack);
			if (exception instanceof Throwable) {
				fail(stack, TypeUtil.toString(A_MESSAGE.getValue(stack)), (Throwable) exception);
			}
			else {
				fail(stack, TypeUtil.toString(A_MESSAGE.getValue(stack)) + "\n"
						+ exception.toString());
			}
		}
		else {
			fail(stack, TypeUtil.toString(A_MESSAGE.getValue(stack)));
		}
	}

	protected boolean executeErrorHandler(VariableStack stack, FlowEvent error)
			throws ExecutionException {
		//No error handling for this
		return false;
	}
}