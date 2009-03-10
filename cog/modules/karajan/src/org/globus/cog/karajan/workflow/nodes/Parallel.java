// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes;

import java.util.Iterator;

import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;

public class Parallel extends FlowContainer {

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		int count = elementCount();
		if (count == 0) {
			complete(stack);
			return;
		}
		// Avoid terminating before all elements are started
		setRunning(stack, count + 1);
		stack.setCaller(this);
		setChildFailed(stack, false);
	}

	public void executeChildren(VariableStack stack) throws ExecutionException {
		int index = 0;
		Iterator i = elements().iterator();
		initializeChannelBuffers(stack);
		synchronized (this) {
			while (i.hasNext()) {
				FlowElement fe = (FlowElement) i.next();
				VariableStack copy = stack.copy();
				copy.enter();
				ThreadingContext.set(copy, ThreadingContext.get(stack).split(index));
				addChannelBuffers(copy);
				startElement(fe, copy);
				index++;
			}
		}
		// Check if all children are done
		if (preDecRunning(stack) == 0) {
			if (!getChildFailed(stack)) {
				post(stack);
			}
		}
	}

	protected void initializeChannelBuffers(VariableStack stack) throws ExecutionException {
		ArgUtil.initializeChannelBuffers(stack);
	}

	protected void addChannelBuffers(VariableStack stack) throws ExecutionException {
		ArgUtil.addChannelBuffers(stack);
	}

	protected void closeBuffers(VariableStack stack) throws ExecutionException {
		ArgUtil.closeBuffers(stack);
	}

	protected final void setRunning(VariableStack stack, int running) {
		stack.getRegs().setIB(running);
	}

	protected final synchronized int preDecRunning(VariableStack stack) {
		return stack.getRegs().preDecIB();
	}

	protected final synchronized int preIncRunning(VariableStack stack) {
		return stack.getRegs().preIncIB();
	}

	protected synchronized void notificationEvent(NotificationEvent e) throws ExecutionException {
		VariableStack stack = e.getStack();
		if (NotificationEventType.EXECUTION_COMPLETED.equals(e.getType())
				|| NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
			closeBuffers(stack);
		}
		if (NotificationEventType.EXECUTION_COMPLETED.equals(e.getType())) {
			stack.leave();
			if (preDecRunning(stack) == 0) {
				if (!getChildFailed(stack)) {
					post(stack);
				}
			}
		}
		else if (NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
			stack.leave();
			if (!getChildFailed(stack)) {
				setChildFailed(stack, true);
				super.notificationEvent(e);
			}
		}
		else {
			super.notificationEvent(e);
		}
	}
}
