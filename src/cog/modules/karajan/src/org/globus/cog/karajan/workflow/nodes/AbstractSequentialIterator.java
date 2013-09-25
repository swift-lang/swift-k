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
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.KarajanIterator;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureIteratorIncomplete;
import org.globus.cog.karajan.workflow.futures.FutureListener;

public abstract class AbstractSequentialIterator extends AbstractIterator implements FutureListener {
	private static final Logger logger = Logger.getLogger(AbstractSequentialIterator.class);

	public static final String SEQ_COMPLETED = "#seqCompleted";

	public void iterate(VariableStack stack, Identifier var, KarajanIterator l)
			throws ExecutionException {
		try {
			if (!l.hasNext()) {
				complete(stack);
				return;
			}
			stack.setVar(VAR, var);
			stack.setVar(ITERATOR, l);
			stack.setVar(var.getName(), l.next());
			stack.setCaller(this);
			setIndex(stack, getArgCount() + 1);

			if (FlowNode.debug) {
				threadTracker.remove(new FNTP(this, ThreadingContext.get(stack)));
			}
			if (elementCount() > getArgCount()) {
				startElement(getArgCount(), stack);
			}
			else {
				complete(stack);
			}
		}
		catch (FutureIteratorIncomplete fii) {
			stack.setVar(VAR, var);
			stack.setVar(ITERATOR, l);
			fii.getFutureIterator().addModificationAction(this, stack);
		}
	}

	public void futureModified(Future f, VariableStack stack) {
		try {
			KarajanIterator it = (KarajanIterator) stack.currentFrame().getVar(ITERATOR);
			if (stack.currentFrame().isDefined(SEQ_COMPLETED)) {
				iterationCompleted(stack, it);
			}
			else {
				iterate(stack, (Identifier) stack.currentFrame().getVar(VAR), it);
			}
		}
		catch (ExecutionException e) {
			failImmediately(stack, e);
		}
	}

	protected final synchronized void nonArgChildCompleted(VariableStack stack)
			throws ExecutionException {
		int childIndex = preIncIndex(stack) - 1;
		if (childIndex > elementCount()) {
			logger.warn(stack.toString());
			logger.warn("Trace: ", new Throwable());
			logger.warn(Thread.currentThread());
		}
		if (childIndex == elementCount()) {
			KarajanIterator l = (KarajanIterator) stack.currentFrame().getVar(ITERATOR);
			iterationCompleted(stack, l);
		}
		else {
			startElement(childIndex, stack);
		}
	}

	protected final void iterationCompleted(VariableStack stack, KarajanIterator l)
			throws ExecutionException {
		try {
			if (!l.hasNext()) {
				complete(stack);
			}
			else {
				if (FlowNode.debug) {
					threadTracker.put(new FNTP(this, ThreadingContext.get(stack)), stack);
				}
				setIndex(stack, getArgCount() + 1);
				Object value = l.next();
				Identifier var = (Identifier) stack.currentFrame().getVar(VAR);
				stack.currentFrame().setVar(var.getName(), value);
				startElement(getArgCount(), stack);
			}
		}
		catch (FutureIteratorIncomplete fii) {
			stack.setVar(ITERATOR, l);
			fii.getFutureIterator().addModificationAction(
					this, stack);
		}
	}

	public void failed(VariableStack stack, ExecutionException e) throws ExecutionException {
		failImmediately(stack, e);
	}
}