//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 6, 2006
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Iterator;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.arguments.VariableArgumentsImpl;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.ChannelSplitter;
import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;

public class ChannelFork extends AbstractSequentialWithArguments {
	public static final Arg A_NAME = new Arg.Positional("name", 0);
	public static final Arg A_COUNT = new Arg.Positional("count", 1);
	
	static {
		setArguments(ChannelFork.class, new Arg[] { A_NAME, A_COUNT });
	}

	protected void argumentsEvaluated(VariableStack stack) throws ExecutionException {
		int count = TypeUtil.toInt(A_COUNT.getValue(stack));
		VariableArguments channel = (VariableArguments) checkClass(A_NAME.getValue(stack),
				VariableArguments.class, "name");
		VariableArguments[] ret;
		if (channel instanceof FutureVariableArguments) {
			ChannelSplitter mux = new ChannelSplitter((FutureVariableArguments) channel, count);
			ret = mux.getOuts();
		}
		else {
			ret = new VariableArguments[count];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new VariableArgumentsImpl();
			}
			Iterator i = channel.iterator();
			while (i.hasNext()) {
				Object o = i.next();
				for (int j = 0; j < ret.length; j++) {
					ret[j].append(o);
				}
			}
		}
		for (int i = 0; i < ret.length; i++) {
			ret(stack, ret[i]);
		}
	}
}
