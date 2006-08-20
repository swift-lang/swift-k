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
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class ChannelFrom extends PartialArgumentsContainer {
	public static final Arg A_NAME = new Arg.Positional("name", 0);

	static {
		setArguments(ChannelFrom.class, new Arg[] { A_NAME, Arg.VARGS });
	}
	
	public ChannelFrom() {
		setQuotedArgs(true);
	}
	
	public void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		Identifier from = TypeUtil.toIdentifier(A_NAME.getValue(stack));
		Arg.Channel channel = new Arg.Channel(from.getName());
		ArgUtil.createChannel(stack, channel, ArgUtil.getVariableReturn(stack));
		super.partialArgumentsEvaluated(stack);
		startRest(stack);
	}
}