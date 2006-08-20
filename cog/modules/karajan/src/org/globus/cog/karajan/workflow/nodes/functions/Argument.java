// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.UnsupportedArgumentException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.SequentialWithArguments;

public class Argument extends SequentialWithArguments {
	public static final Arg A_NAME = new Arg.Optional("name", null);
	public static final Arg A_VALUE = new Arg.Positional("value", 0);
	
	static {
		setArguments(Argument.class, new Arg[] { A_NAME, A_VALUE });
	}

	public Argument() {
		setAcceptsInlineText(true);
	}

	public void post(VariableStack stack) throws ExecutionException {
		Object value = A_VALUE.getValue(stack);
		String name = (String) A_NAME.getValue(stack);
		if (name != null) {
			try {
				ArgUtil.getNamedReturn(stack).add(name.toLowerCase(), value);
			}
			catch (UnsupportedArgumentException e) {
				throw new ExecutionException(e.getMessage());
			}
		}
		else {
			ArgUtil.getVariableReturn(stack).append(value);
		}
		super.post(stack);
	}
}