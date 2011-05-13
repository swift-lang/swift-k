// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.Regs;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Sequential extends FlowContainer {
	private static final Logger logger = Logger.getLogger(Sequential.class);

	protected void executeChildren(VariableStack stack) throws ExecutionException {
		stack.setCaller(this);
		setIndex(stack, 0);
		startNext(stack);
	}

	public void completed(VariableStack stack) throws ExecutionException {
		startNext(stack);
	}

	protected void startNext(VariableStack stack) throws ExecutionException {
		if (!moreToExecute(stack)) {
			post(stack);
			return;
		}
		int index = preIncIndex(stack) - 1;
		startElement(getElement(index), stack);
	}

	protected final void startElement(FlowElement fn, VariableStack stack)
			throws ExecutionException {
		if (fn.isSimple()) {
			fn.executeSimple(stack);
			completed(stack);
		}
		else {
			super.startElement(fn, stack);
		}
	}

	protected boolean moreToExecute(VariableStack stack) throws ExecutionException {
	    int ec = elementCount();
		if (ec == 0) {
			return false;
		}
		int index = getIndex(stack);
		if (index == ec || index == Integer.MAX_VALUE) {
			return false;
		}
		return true;
	}

	/**
	 * Aborts the execution of any further sub-elements
	 */
	protected void exit(VariableStack stack) throws ExecutionException {
	    if (logger.isDebugEnabled()) {
	    	logger.debug(this + " exit");
	    }
		stack.getRegs().setIA(Integer.MAX_VALUE);
	}

	protected final int preDecIndex(VariableStack stack) {
	    Regs r = stack.getRegs();
	    synchronized (r) {
	    	return r.preDecIA();
	    }
	}

	protected final int preIncIndex(VariableStack stack) {
	    Regs r = stack.getRegs();
        synchronized (r) {
            return r.preIncIA();
        }
	}

	protected final void setIndex(VariableStack stack, int value) {
		stack.getRegs().setIA(value);
	}

	protected final int getIndex(VariableStack stack) {
		return stack.getRegs().getIA();
	}
}
