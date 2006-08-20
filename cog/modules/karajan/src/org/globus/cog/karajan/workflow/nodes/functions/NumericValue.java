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

public class NumericValue extends AbstractFunction implements DirectExecution {
	public static final Arg A_VALUE = new Arg.Positional("value");

	static {
		setArguments(NumericValue.class, new Arg[] { A_VALUE });
	}

	public NumericValue() {
		this.setAcceptsInlineText(true);
	}

	public Object function(VariableStack stack) throws ExecutionException {
		double value = 0;
		String strval = TypeUtil.toString(A_VALUE.getValue(stack));
		value = TypeUtil.toDouble(A_VALUE.getValue(stack));
		Double ret = new Double(value);
		setValue(ret);
		setSimple(true);
		return ret;
	}
}