// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.Condition;
import org.globus.cog.karajan.workflow.ExecutionException;

public class ConditionNode extends SequentialWithArguments {
	public static final Arg A_VALUE = new Arg.Positional("value", 0);

	static {
		setArguments(ConditionNode.class, new Arg[] { A_VALUE });
	}

	public void post(VariableStack stack) throws ExecutionException {
		Condition condition = (Condition) stack.getVar("#condition");
		condition.setValue(A_VALUE.getValue(stack));
		super.post(stack);
	}
}