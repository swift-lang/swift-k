//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 8, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Guard extends Sequential {
	public static final String FAILED_EVENT = "##failed-event";

	public void pre(VariableStack stack) throws ExecutionException {
		if (elementCount() != 2) {
			throw new ExecutionException("Guard must have exactly two sub-elements");
		}
		super.pre(stack);
	}

	public void completed(VariableStack stack) throws ExecutionException {
		if (getChildFailed(stack)) {
			super.failed(stack, (ExecutionException) stack.getVar(FAILED_EVENT));
		}
		else {
			super.completed(stack);
		}
	}

	public void failed(VariableStack stack, ExecutionException e) throws ExecutionException {
		if (this.getIndex(stack) == 1) {
			stack.setVar(FAILED_EVENT, e);
			this.setChildFailed(stack, true);
			startNext(stack);
			return;
		}
		else {
			super.failed(stack, e);
		}
	}
}
