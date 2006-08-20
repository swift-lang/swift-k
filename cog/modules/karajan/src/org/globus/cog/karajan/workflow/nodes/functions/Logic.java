// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 11, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Logic extends FunctionsCollection {
	static {
		setArguments("sys_or", new Arg[] { Arg.VARGS });
		addAlias("sys___pipe_", "sys_or");
	}

	public boolean sys_or(VariableStack stack) throws ExecutionException {
		boolean ret = false;
		Object[] args = Arg.VARGS.asArray(stack);
		for (int i = 0; i < args.length; i++) {
			ret = ret || TypeUtil.toBoolean(args[i]);
		}
		return ret;
	}
	
	public static final Arg PA_VALUE = new Arg.Positional("value");

	static {
		setArguments("sys_not", new Arg[] { PA_VALUE });
		addAlias("sys___bang_", "sys_not");
	}

	public boolean sys_not(VariableStack stack) throws ExecutionException {
		return !TypeUtil.toBoolean(PA_VALUE.getValue(stack));
	}
	

	static {
		setArguments("sys_and", new Arg[] { Arg.VARGS });
		addAlias("sys___amp_", "sys_and");
	}
	
	public boolean sys_and(VariableStack stack) throws ExecutionException {
		boolean ret = true;
		Object[] args = Arg.VARGS.asArray(stack);
		for (int i = 0; i < args.length; i++) {
			ret = ret && TypeUtil.toBoolean(args[i]);
		}
		return ret;
	}

	public boolean sys_false(VariableStack stack) throws ExecutionException {
		return false;
	}

	public boolean sys_true(VariableStack stack) throws ExecutionException {
		return true;
	}
}
