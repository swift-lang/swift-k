// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 26, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class CacheOn extends CacheNode {
	public static final Arg A_VALUE = new Arg.Positional("value", 0);

	static {
		setArguments(CacheOn.class, new Arg[] { A_VALUE });
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		List key = new LinkedList();
		key.add(getProperty(UID));
		key.add(A_VALUE.getValue(stack));
		cpre(key, stack);
	}

}
