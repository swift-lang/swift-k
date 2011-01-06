// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 26, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Exclusive extends PartialArgumentsContainer {
	public static final String LOCKS = "#exclusive:locks";
	public static final String ON = "#on";

	public static final Arg A_ON = new Arg.Optional("on");

	static {
		setArguments(Exclusive.class, new Arg[] { A_ON });
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		Map locks = getLocks(stack);
		Object on = A_ON.getValue(stack, Object.class);
		stack.setVar(ON, on);
		if (testAndAdd(locks, on, stack)) {
			startRest(stack);
		}
	}

	protected boolean testAndAdd(Map locks, Object on, VariableStack stack) {
		synchronized (locks) {
			LinkedList stacks = (LinkedList) locks.get(on);
			boolean first;
			if (stacks == null) {
				first = true;
				stacks = new LinkedList();
				locks.put(on, stacks);
			}
			else {
				first = false;
				stacks.add(stack);
			}
			return first;
		}
	}

	private Map getLocks(VariableStack stack) {
		synchronized (stack.getExecutionContext()) {
			Map locks = (Map) stack.getGlobal(LOCKS);
			if (locks == null) {
				locks = new HashMap();
				stack.setGlobal(LOCKS, locks);
			}
			return locks;
		}
	}

	protected void _finally(VariableStack stack) throws ExecutionException {
		Map locks = getLocks(stack);
		VariableStack next = null;
		synchronized (locks) {
			Object on = stack.currentFrame().getVar(ON);
			LinkedList instances = (LinkedList) locks.get(on);
			if (instances.size() > 0) {
				next = (VariableStack) instances.removeFirst();
			}
			else {
				locks.remove(on);
			}
		}
		if (next != null) {
			pre(next);
		}
		super._finally(stack);
	}
}
