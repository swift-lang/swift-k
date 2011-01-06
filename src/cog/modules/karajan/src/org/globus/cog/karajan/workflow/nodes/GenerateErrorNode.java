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
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.FlowEvent;

public class GenerateErrorNode extends SequentialWithArguments {

	public static final Arg EXCEPTION = new Arg.Positional("exception");

	static {
		setArguments(GenerateErrorNode.class, new Arg[] { EXCEPTION });
	}

	public void post(VariableStack stack) throws ExecutionException {
			Object exc = EXCEPTION.getValue(stack);
			if (exc instanceof String) {
				fail(stack, (String) exc);
			}
			else if (exc instanceof ExecutionException) {
				ExecutionException prev = (ExecutionException) exc;
				throw prev;
			}
			else if (exc instanceof Throwable) {
				Throwable t = (Throwable) exc;
				fail(stack, t.getMessage(), t);
			}
			else {
				fail(stack, String.valueOf(exc));
			}
	}

	protected boolean executeErrorHandler(VariableStack stack, FlowEvent error)
			throws ExecutionException {
		// No error handling for this
		return false;
	}
}