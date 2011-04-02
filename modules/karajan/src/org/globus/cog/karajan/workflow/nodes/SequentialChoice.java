// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 6, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;

public class SequentialChoice extends Sequential {
	private static final Logger logger = Logger.getLogger(SequentialChoice.class);

	public static final Arg A_BUFFER = new Arg.Optional("buffer", Boolean.TRUE);

	private boolean buffer;

	public static final String LAST_FAILURE = "##choice:lastFailure";

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		initializeArgBuffers(stack);
	}

	protected void childCompleted(VariableStack stack) throws ExecutionException {
		if (buffer) {
			commitBuffers(stack);
		}
		super.post(stack);
	}

	protected void commitBuffers(VariableStack stack) throws ExecutionException {
		ArgUtil.getNamedReturn(stack).merge(ArgUtil.getNamedArguments(stack));
		ArgUtil.getVariableReturn(stack).merge(ArgUtil.getVariableArguments(stack));
		Iterator i = ArgUtil.getDefinedChannels(stack).iterator();
		while (i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			channel.getReturn(stack).merge(channel.get(stack));
		}
	}

	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		if (NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
			VariableStack stack = e.getStack();
			FailureNotificationEvent fne = (FailureNotificationEvent) e;
			stack.setVar(LAST_FAILURE, e);
			stack.setVar("exception", fne.getException());
			initializeArgBuffers(stack);
			startNext(stack);
		}
		else {
			super.notificationEvent(e);
		}
	}

	protected void initializeArgBuffers(VariableStack stack) throws ExecutionException {
		if (buffer) {
			ArgUtil.initializeNamedArguments(stack);
			ArgUtil.initializeVariableArguments(stack);
			ArgUtil.duplicateChannels(stack);
		}
	}

	public void post(VariableStack stack) throws ExecutionException {
		failImmediately(stack, (FailureNotificationEvent) stack.getVar(LAST_FAILURE));
	}

	protected void initializeStatic() throws KarajanRuntimeException {
		super.initializeStatic();
		buffer = TypeUtil.toBoolean(A_BUFFER.getStatic(this));
	}
}