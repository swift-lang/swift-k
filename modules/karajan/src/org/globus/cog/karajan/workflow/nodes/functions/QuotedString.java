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
import org.globus.cog.karajan.workflow.ExecutionException;

public class QuotedString extends AbstractFunction {
	public static final Arg A_VALUE = new Arg.Positional("value");

	static {
		setArguments(QuotedString.class, new Arg[] { A_VALUE });
	}

	public QuotedString() {
		this.setAcceptsInlineText(true);
		this.setQuotedArgs(true);
	}
	
	public Object function(VariableStack stack) throws ExecutionException {
		String value = null;
		if (value == null) {
			value = TypeUtil.toString(A_VALUE.getValue(stack));
		}
		setValue(value);
		return value;
	}
}