//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 27, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.Sequential;

public class Args extends Sequential {
	public final void post(VariableStack stack) throws ExecutionException {
		ArgUtil.getNamedReturn(stack).set(ArgUtil.getNamedArguments(stack));
		ArgUtil.getVariableReturn(stack).set(ArgUtil.getVariableArguments(stack));
		super.post(stack);
	}
}
