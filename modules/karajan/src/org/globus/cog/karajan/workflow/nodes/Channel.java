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
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ChannelIdentifier;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;

public class Channel extends SequentialWithArguments {
	public static final Arg A_NAME = new Arg.Positional("name", 0);
	
	private String to, from;

	static {
		setArguments(Channel.class, new Arg[] { A_NAME });
	}
	
	public Channel() {
		setQuotedArgs(true);
	}

	public void post(VariableStack stack) throws ExecutionException {
		if (A_NAME.isPresent(stack)) {
			ret(stack, new ChannelIdentifier(TypeUtil.toString(A_NAME.getValue(stack))));
		}
		else {
			ret(stack, new FutureVariableArguments());
		}
		super.post(stack);
	}
}