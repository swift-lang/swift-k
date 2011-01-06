//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 17, 2005
 */
package org.globus.cog.karajan.workflow.service;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.KarajanProperties;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.ControlEvent;
import org.globus.cog.karajan.workflow.events.ControlEventType;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;

public class RemoteExecutionContext extends ExecutionContext {
	private final transient FlowElement caller;
	private final InstanceContext ic;

	public RemoteExecutionContext(InstanceContext ic, int callerUID, KarajanProperties properties) {
		super(ic.getTree(), properties);
		this.ic = ic;
		this.caller = new RemoteCaller(ic, callerUID);
	}

	public void start(VariableStack stack, FlowElement fe) {
		setStartTime(System.currentTimeMillis());
		ic.registerExecutionContext(this);
		setGlobals(stack);
		defineKernel(stack);
		stack.setCaller(this);
		ThreadingContext.set(stack, new ThreadingContext());
		EventBus.post(fe, new ControlEvent(null, ControlEventType.START, stack));
	}

	public void start(VariableStack stack) {
		start(stack, stack.getExecutionContext().getTree().getRoot());
	}

	public void event(Event e) throws ExecutionException {
		caller.event(e);
		if (e instanceof NotificationEvent) {
			notificationEvent((NotificationEvent) e);
		}
	}

	private void notificationEvent(NotificationEvent e) {
		if (NotificationEventType.EXECUTION_COMPLETED.equals(e.getType())
				|| NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
			setDone();
			getStateManager().stop();
			ic.unregisterExecutionContext(this);
		}
	}
}
