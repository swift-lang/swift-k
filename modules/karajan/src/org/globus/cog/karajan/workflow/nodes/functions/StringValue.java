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
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.DirectExecution;
import org.globus.cog.karajan.workflow.ExecutionException;

public class StringValue extends AbstractFunction implements DirectExecution {
	public static final Arg A_VALUE = new Arg.Positional("value");

	static {
		setArguments(StringValue.class, new Arg[] { A_VALUE });
	}

	protected void initializeStatic() {
		super.initializeStatic();
		Object value;
		if (A_VALUE.isPresentStatic(this)
				&& ((value = A_VALUE.getStatic(this))) instanceof String) {
			setValue(value);
			setSimple(true);
		}
	}

	public StringValue() {
		this.setAcceptsInlineText(true);
	}

	public Object function(VariableStack stack) throws ExecutionException {
		return TypeUtil.toString(A_VALUE.getValue(stack));
	}
}