// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.griphyn.vdl.karajan.lib;

import java.util.Arrays;
import java.util.List;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.Condition;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.Sequential;
import org.globus.cog.karajan.workflow.nodes.While;

public class InfiniteCountingWhile extends Sequential {
	public static final String VAR = "##infinitecountingwhile:var";

	public InfiniteCountingWhile() {
		setOptimize(false);
	}

	public void pre(VariableStack stack) throws ExecutionException {
		stack.setVar("#condition", new Condition());
		ThreadingContext tc = (ThreadingContext)stack.getVar("#thread");
		stack.setVar("#iteratethread", tc);
		stack.setVar("#thread", tc.split(0));
		stack.setVar(VAR, "$");
		String counterName = (String)stack.getVar(VAR);
		stack.setVar(counterName, Arrays.asList(new Integer[] {new Integer(0)}));
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
			// starting new iteration
			setIndex(stack, 1);
			fn = getElement(0);

			String counterName = (String) stack.getVar(VAR);
			@SuppressWarnings("unchecked")
            List<Integer> l = (List<Integer>) stack.getVar(counterName);
			Integer wrappedi = l.get(0);
			int i = wrappedi.intValue();
			i++;
			ThreadingContext tc = (ThreadingContext)stack.getVar("#iteratethread");
			stack.setVar("#thread", tc.split(i));
			stack.setVar(counterName, Arrays.asList(new Integer[] {new Integer(i)}));
		}
		else {
			fn = getElement(index++);
			setIndex(stack, index);
		}
		startElement(fn, stack);
	}
	
    public void failed(VariableStack stack, ExecutionException e)
            throws ExecutionException {
        if (e instanceof While.Break) {
        	complete(stack);
        	return;
        }
        if (e instanceof While.Continue) {
        	setIndex(e.getStack(), 0);
            startNext(e.getStack());
            return;
        }
    }
}
