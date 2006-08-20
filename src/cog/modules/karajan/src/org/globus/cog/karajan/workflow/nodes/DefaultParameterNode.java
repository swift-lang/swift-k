// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class DefaultParameterNode extends SequentialWithArguments {
	public static final Arg A_NAME = new Arg.Positional("name", 0);
	public static final Arg A_VALUE = new Arg.Positional("value", 1);

	static {
		setArguments(DefaultParameterNode.class, new Arg[] { A_NAME, A_VALUE });
	}
	
	public DefaultParameterNode() {
		setQuotedArgs(true);
	}

	public void post(VariableStack stack) throws ExecutionException {
		Identifier ident = TypeUtil.toIdentifier(A_NAME.getValue(stack));
		if (!stack.isDefined(ident.getName())) {
			Object value = A_VALUE.getValue(stack);
			if (value instanceof Identifier) {
				stack.parentFrame().setVar(ident.getName(), ((Identifier) value).getValue(stack));
			}
			else {
				stack.parentFrame().setVar(ident.getName(), value);
			}
		}
		super.post(stack);
	}
}
