// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.DirectExecution;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;

public class Sequential extends FlowContainer {
	private static final Logger logger = Logger.getLogger(Sequential.class);

	protected void executeChildren(VariableStack stack) throws ExecutionException {
		stack.setCaller(this);
		if (isOptimizable() && (elementCount() == 1)) {
			executeSingle(stack);
		}
		else {
			setIndex(stack, 0);
			startNext(stack);
		}
	}

	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		if (NotificationEventType.EXECUTION_COMPLETED.equals(e.getType())) {
			childCompleted(e.getStack());
		}
		else {
			super.notificationEvent(e);
		}
	}

	protected void childCompleted(VariableStack stack) throws ExecutionException {
		if (isOptimizable() && (elementCount() == 1)) {
			post(stack);
		}
		else {
			startNext(stack);
		}
	}

	protected void startNext(VariableStack stack) throws ExecutionException {
		if (!moreToExecute(stack)) {
			post(stack);
			return;
		}
		int index = getIndex(stack);
		preIncIndex(stack);
		if (FlowNode.debug) {
			threadTracker.remove(new FNTP(this, ThreadingContext.get(stack)));
		}
		startElement(getElement(index), stack);
	}

	protected void executeSingle(VariableStack stack) throws ExecutionException {
		if (FlowNode.debug) {
			threadTracker.remove(new FNTP(this, ThreadingContext.get(stack)));
		}
		FlowElement fn = getElement(0);
		if (fn instanceof ExtendedFlowElement && ((ExtendedFlowElement) fn).isSimple()) {
			((ExtendedFlowElement) fn).executeSimple(stack);
			post(stack);
		}
		else if (fn instanceof DirectExecution) {
			((DirectExecution) fn).start(stack);
		}
		else {
			super.startElement(fn, stack);
		}
	}

	protected final void startElement(FlowElement fn, VariableStack stack)
			throws ExecutionException {
		if (FlowNode.debug) {
			threadTracker.remove(new FNTP(this, ThreadingContext.get(stack)));
		}
		if (fn instanceof ExtendedFlowElement && ((ExtendedFlowElement) fn).isSimple()) {
			((ExtendedFlowElement) fn).executeSimple(stack);
			childCompleted(stack);
		}
		else if (fn instanceof DirectExecution) {
			((DirectExecution) fn).start(stack);
		}
		else {
			super.startElement(fn, stack);
		}
	}

	protected boolean moreToExecute(VariableStack stack) throws ExecutionException {
		if (elementCount() == 0) {
			return false;
		}
		int index = getIndex(stack);
		if (index == elementCount() || index == Integer.MAX_VALUE) {
			return false;
		}
		return true;
	}

	/**
	 * Aborts the execution of any further sub-elements
	 */
	protected void exit(VariableStack stack) throws ExecutionException {
		logger.debug(this + " exit");
		stack.getRegs().setIA(Integer.MAX_VALUE);
	}

	protected final synchronized int preDecIndex(VariableStack stack) {
		return stack.getRegs().preDecIA();
	}

	protected final synchronized int preIncIndex(VariableStack stack) {
		return stack.getRegs().preIncIA();
	}

	protected final void setIndex(VariableStack stack, int value) {
		stack.getRegs().setIA(value);
	}

	protected final int getIndex(VariableStack stack) {
		return stack.getRegs().getIA();
	}
}
