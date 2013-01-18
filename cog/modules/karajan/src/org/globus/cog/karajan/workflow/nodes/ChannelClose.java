//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 6, 2006
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.Future;

public class ChannelClose extends AbstractSequentialWithArguments {
    public static final Arg A_NAME = new Arg.Positional("name", 0);
    
	static {
		setArguments(ChannelClose.class, new Arg[] { A_NAME });
	}

	protected void argumentsEvaluated(VariableStack stack) throws ExecutionException {
		VariableArguments vargs = (VariableArguments) checkClass(A_NAME.getValue(stack),
				VariableArguments.class, "name");
		if (vargs instanceof Future) {
			((Future) vargs).close();
		}
	}
}
