// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 29, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;

public class ParallelChoice extends Parallel {
	private static final Logger logger = Logger.getLogger(ParallelChoice.class);
	
	public static final Arg A_BUFFER = new Arg.Optional("buffer", Boolean.TRUE);

	public static final String COMPLETED = "##choice:completed";

	private boolean buffer;

	protected void initializeStatic() throws KarajanRuntimeException {
		super.initializeStatic();
		buffer = TypeUtil.toBoolean(A_BUFFER.getStatic(this));
	}

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		int count = elementCount();
		if (count == 0) {
			complete(stack);
			return;
		}
		stack.setVar(COMPLETED, false);
		stack.setCaller(this);
	}

	public void executeChildren(VariableStack stack) throws ExecutionException {
		int index = 0;
		Iterator i = elements().iterator();
		synchronized (this) {
			while (i.hasNext()) {
				FlowElement fe = (FlowElement) i.next();
				VariableStack copy = stack.copy();
				copy.enter();
				if (buffer) {
					ArgUtil.initializeNamedArguments(copy);
					ArgUtil.initializeVariableArguments(copy);
					ArgUtil.duplicateChannels(copy);
				}
				ThreadingContext.set(copy, ThreadingContext.get(stack).split(index++));
				startElement(fe, copy);
			}
		}
	}

	protected synchronized void notificationEvent(NotificationEvent e) throws ExecutionException {
		VariableStack stack = e.getStack();
		if (NotificationEventType.EXECUTION_COMPLETED.equals(e.getType())) {
			NamedArguments named = null;
			VariableArguments vargs = null;
			Map channels = null;
			

			if (buffer) {
				named = ArgUtil.getNamedArguments(stack);
				vargs = ArgUtil.getVariableArguments(stack);
				Set dchannels = ArgUtil.getDefinedChannels(stack);
				channels = new Hashtable();
				Iterator i = dchannels.iterator();
				while (i.hasNext()) {
					Arg.Channel channel = (Arg.Channel) i.next();
					channels.put(channel, ArgUtil.getChannelArguments(stack, channel));
				}
			}

			stack.leave();
			if (this != stack.getCaller()) {
				logger.error("Stack inconsistency detected");
				logger.error("Event came from " + e.getFlowElement());
				System.err.println(this);
				stack.dumpAll();
				failImmediately(stack, "Stack inconsistency");
				return;
			}
			boolean complete = stack.currentFrame().getBooleanVar(COMPLETED);
			stack.setVar(COMPLETED, true);
			if (!complete) {
				if (buffer) {
					VariableArguments ret = ArgUtil.getVariableReturn(stack);
					ArgUtil.getNamedReturn(stack).merge(named);
					ArgUtil.getVariableReturn(stack).merge(vargs);
					Iterator i = channels.keySet().iterator();
					while (i.hasNext()) {
						Arg.Channel channel = (Arg.Channel) i.next();
						channel.getReturn(stack).merge((VariableArguments) channels.get(channel));
					}
				}
				// singnal an abort to all sub-threads. They are not needed
				// any more
                stack.setVar("#abort", true);
				stack.getExecutionContext().getStateManager().abortContext(
						ThreadingContext.get(stack));
				post(stack);
			}
		}
		else if (NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
			stack.leave();
			if (!stack.currentFrame().getBooleanVar(COMPLETED)) {
				stack.setVar(COMPLETED, true);
				failImmediately(stack, (FailureNotificationEvent) e);
			}
		}
		else {
			super.notificationEvent(e);
		}
	}
}