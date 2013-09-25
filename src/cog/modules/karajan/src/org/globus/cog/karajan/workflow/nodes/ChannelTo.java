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
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class ChannelTo extends PartialArgumentsContainer {
	public static final Arg A_NAME = new Arg.Positional("name", 0);

	static {
		setArguments(ChannelTo.class, new Arg[] { A_NAME, Arg.VARGS });
	}
	
	public ChannelTo() {
		setQuotedArgs(true);
	}

	public void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		Identifier to = TypeUtil.toIdentifier(A_NAME.getValue(stack));
		Arg.Channel cto = new Arg.Channel(to.getName());
		VariableArguments args;
		if (ArgUtil.isReceiverPresent(stack, cto)) {
			args = ArgUtil.getChannelReturn(stack, cto);
		}
		else {
			throw new ExecutionException("No such channel: " + to);
		}
		super.partialArgumentsEvaluated(stack);
		ArgUtil.setVariableArguments(stack, args);
		startRest(stack);
	}
}