// ----------------------------------------------------------------------
	//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 19, 2005
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.Sequential;

public class Sum extends Sequential {

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		VariableArgumentsOperator op = new VariableArgumentsOperator() {
			public Object initialValue() {
				return new Double(0);
			}

			public Object update(Object old, Object value) {
				return new Double(((Double) old).doubleValue() + TypeUtil.toDouble(value));
			}

			public boolean isCommutative() {
				return true;
			}
		};
		ArgUtil.setVariableArguments(stack, op);
	}

	public void post(VariableStack stack) throws ExecutionException {
		VariableArgumentsOperator vao = (VariableArgumentsOperator) ArgUtil.getVariableArguments(stack);
		ret(stack, vao.getValue());
		super.post(stack);
	}
}