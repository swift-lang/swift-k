// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 19, 2005
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.Sequential;

public class Time extends Sequential {
	public static final String START = "#start";
	
	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		stack.setVar(START, new Long(System.currentTimeMillis()));
	}

	public void post(VariableStack stack) throws ExecutionException {
		long start = ((Long) stack.getVar(START)).longValue();
		ret(stack, new Long(System.currentTimeMillis() - start));
		super.post(stack);
	}
}