// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.Condition;
import org.globus.cog.karajan.workflow.ExecutionException;

public class While extends Sequential {
	public While() {
		setOptimize(false);
	}

	public void pre(VariableStack stack) throws ExecutionException {
		stack.setVar("#condition", new Condition());
		super.pre(stack);
	}

	protected void startNext(VariableStack stack) throws ExecutionException {
		if (stack.isDefined("#abort")) {
			abort(stack);
			return;
		}
		int index = getIndex(stack);
		if (elementCount() == 0) {
			post(stack);
			return;
		}
		FlowElement fn = null;

		Condition condition = (Condition) stack.getVar("#condition");
		if (condition.getValue() != null) {
			boolean cond = TypeUtil.toBoolean(condition.getValue());
			if (!cond) {
				post(stack);
				return;
			}
		}
		if (index >= elementCount()) {
			setIndex(stack, 1);
			fn = (FlowElement) getElement(0);
		}
		else {
			fn = (FlowElement) getElement(index++);
			setIndex(stack, index);
		}
		startElement(fn, stack);
	}
	
	public void failed(VariableStack stack, ExecutionException e) throws ExecutionException {
		if (e instanceof Break) {
			complete(e.getStack());
			return;
		}
		if (e instanceof Continue) {
			setIndex(e.getStack(), 0);
			startNext(e.getStack());
			return;
		}
		super.failed(stack, e);
	}
	
	public static class Break extends ExecutionException {
		
	}
	
	public static class Continue extends ExecutionException {
		
	}
}