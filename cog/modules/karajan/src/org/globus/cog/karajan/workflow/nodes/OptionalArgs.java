// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 8, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.OptionalArgument;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class OptionalArgs extends SequentialWithArguments {

	private String to, from;

	static {
		setArguments(OptionalArgs.class, new Arg[] { Arg.VARGS });
	}
	
	public OptionalArgs() {
		setQuotedArgs(true);
	}

	public void post(VariableStack stack) throws ExecutionException {
		Object[] vargs = Arg.VARGS.asArray(stack);
		for (int i = 0; i < vargs.length; i++) {
			ret(stack, new OptionalArgument(TypeUtil.toString(vargs[i])));
		}
		super.post(stack);
	}
}