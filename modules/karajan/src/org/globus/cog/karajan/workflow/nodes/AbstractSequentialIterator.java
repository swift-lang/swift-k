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
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.FutureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;
import org.globus.cog.karajan.workflow.events.ProgressMonitoringEvent;
import org.globus.cog.karajan.workflow.futures.FutureIteratorIncomplete;

public abstract class AbstractSequentialIterator extends AbstractIterator {
	private static final Logger logger = Logger.getLogger(AbstractSequentialIterator.class);

	public static final int SEQ_COMPLETED = 1;

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
			stack.setVar(CALLER, this);
			setIndex(stack, getArgCount() + 1);

			if (stack.getExecutionContext().isMonitoringEnabled()) {
				fireMonitoringEvent(new ProgressMonitoringEvent(this,
						ProgressMonitoringEvent.LOOP_PROGRESS, stack, l.count(), 0));
			}
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
			fii.getFutureIterator().addModificationAction(this,
					new FutureNotificationEvent(ITERATE, this, fii.getFutureIterator(), stack));
		}
	}

	protected final synchronized void nonArgChildCompleted(VariableStack stack)
			throws ExecutionException {
		int childIndex = preIncIndex(stack) - 1;
		if (childIndex > elementCount()) {
			logger.debug(stack.toString());
			logger.debug("Trace: ", new Throwable());
			logger.debug(Thread.currentThread());
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
		if (stack.getExecutionContext().isMonitoringEnabled()) {
			this.fireMonitoringEvent(new ProgressMonitoringEvent(this,
					ProgressMonitoringEvent.LOOP_PROGRESS, stack, l.count(), l.current()));
		}
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
					this,
					new FutureNotificationEvent(SEQ_COMPLETED, this, fii.getFutureIterator(), stack));
		}
	}

	public final void notificationEvent(NotificationEvent e) throws ExecutionException {
		if (FutureNotificationEvent.FUTURE_MODIFIED.equals(e.getType())) {
			VariableStack stack = e.getStack();
			FutureNotificationEvent fne = (FutureNotificationEvent) e;
			if (fne.getSubtype() == SEQ_COMPLETED) {
				iterationCompleted(stack, (KarajanIterator) stack.getVar(ITERATOR));
				return;
			}
			else if (fne.getSubtype() == ITERATE) {
				iterate(stack, (KarajanIterator) stack.getVar(ITERATOR));
				return;
			}
			else {
				throw new ExecutionException("Unknown future notification event subtype: "
						+ fne.getSubtype());
			}
		}
		else if (NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
			failImmediately(e.getStack(), (FailureNotificationEvent) e);
			return;
		}
		super.notificationEvent(e);
	}
}