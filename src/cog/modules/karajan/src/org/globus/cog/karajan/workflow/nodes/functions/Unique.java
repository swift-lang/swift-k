// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 19, 2005
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.util.HashSet;
import java.util.Set;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.Sequential;

public class Unique extends Sequential {

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		ArgUtil.setVariableArguments(stack, new Op(Arg.VARGS.getReturn(stack)));
	}

	public void post(VariableStack stack) throws ExecutionException {
		super.post(stack);
	}

	private static class Op extends VariableArgumentsOperator {
		public Set set;
		public VariableArguments vargs;

		public Op(VariableArguments vargs) {
			this.set = new HashSet();
			this.vargs = vargs;
		}

		public Object initialValue() {
			return null;
		}

		public Object update(Object old, Object value) {
			// this should be synchronized by now
			if (!set.contains(value)) {
				set.add(value);
				vargs.append(value);
			}
			return null;
		}

		public boolean isCommutative() {
			return true;
		}
	}
}