// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 21, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.KarajanIterator;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureIteratorIncomplete;

public abstract class AbstractParallelIterator extends AbstractIterator {
	public static final Logger logger = Logger.getLogger(AbstractParallelIterator.class);
	
	public void futureModified(Future f, VariableStack stack) {
		KarajanIterator iter = (KarajanIterator) stack.currentFrame().getVar(ITERATOR);
		try {
			citerate(stack, (Identifier) stack.currentFrame().getVar(VAR), iter);
		}
		catch (ExecutionException e) {
			failImmediately(stack, e);
		}
	}


	public void iterate(VariableStack stack, Identifier var, KarajanIterator i)
			throws ExecutionException {
		if (elementCount() > 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("iterateParallel: " + stack.parentFrame());
			}
			stack.setVar(VAR, var);
			setChildFailed(stack, false);
			stack.setCaller(this);
			initializeChannelBuffers(stack);
			setRunning(stack, 1);
			citerate(stack, var, i);
		}
		else {
			complete(stack);
		}
	}

	protected void executeChildren(VariableStack stack) throws ExecutionException {
		if (elementCount() == 0) {
			return;
		}
		super.executeChildren(stack);
	}

	protected void citerate(VariableStack stack, Identifier var, KarajanIterator i)
			throws ExecutionException {
		try {
			while (i.hasNext()) {
				Object value = i.next();
				VariableStack copy = stack.copy();
				copy.enter();
				ThreadingContext.set(copy, ThreadingContext.get(copy).split(i.current()));
				setIndex(copy, getArgCount());
				setArgsDone(copy);
				copy.setVar(var.getName(), value);
				int r = preIncRunning(stack);
				addChannelBuffers(copy);
				startElement(getArgCount(), copy);
			}
			if (FlowNode.debug) {
				threadTracker.remove(new FNTP(this, ThreadingContext.get(stack)));
			}
			// Now make sure all iterations have not completed
			int left = preDecRunning(stack);
			if (left == 0) {
				complete(stack);
			}
		}
		catch (FutureIteratorIncomplete fii) {
			synchronized (stack.currentFrame()) {
				// if this is defined, then resume from iterate
				stack.setVar(ITERATOR, i);
			}
			fii.getFutureIterator().addModificationAction(this, stack);
		}
	}

	protected void initializeChannelBuffers(VariableStack stack) throws ExecutionException {
		ArgUtil.initializeChannelBuffers(stack);
	}

	protected void addChannelBuffers(VariableStack stack) throws ExecutionException {
		ArgUtil.addChannelBuffers(stack);
	}

	/**
	 * This must be called before a leave
	 */
	protected void closeBuffers(VariableStack stack) throws ExecutionException {
		ArgUtil.closeBuffers(stack);
	}

	protected void iterationCompleted(VariableStack stack) throws ExecutionException {
		closeBuffers(stack);
		stack.leave();
		int running = preDecRunning(stack);
		if (running == 0) {
			complete(stack);
		}
	}

	protected void nonArgChildCompleted(VariableStack stack) throws ExecutionException {
		int childIndex = preIncIndex(stack);
		if (childIndex == elementCount()) {
			iterationCompleted(stack);
		}
		else {
			startElement(childIndex, stack);
		}
	}

	protected boolean testAndSetChildFailed(VariableStack stack) {
		StackFrame parent = stack.parentFrame();
		synchronized (parent) {
			boolean value = parent.getRegs().getBB();
			if (!value) {
				parent.getRegs().setBB(true);
			}
			return value;
		}
	}

	public void failed(VariableStack stack, ExecutionException e) throws ExecutionException {
		if (!testAndSetChildFailed(stack)) {
			if (stack.parentFrame().isDefined(VAR)) {
				closeBuffers(stack);
				stack.leave();
			}
			failImmediately(stack, e);
		}
	}
}