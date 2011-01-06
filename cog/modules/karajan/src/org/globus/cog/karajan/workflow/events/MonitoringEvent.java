/*
 * Created on Aug 27, 2004
 *  
 */
package org.globus.cog.karajan.workflow.events;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public abstract class MonitoringEvent extends Event {
	
	private static final Logger logger = Logger.getLogger(MonitoringEvent.class);

	private ThreadingContext thread;
	private MonitoringEventType type;
	private VariableStack stack;

	public MonitoringEvent(EventClass cls, FlowElement flowElement, MonitoringEventType type, VariableStack stack) {
		super(cls, flowElement, Priority.LOW);
		this.type = type;
		this.stack = stack;
		try {
			thread = ThreadingContext.get(stack);
		}
		catch (VariableNotFoundException e) {
			logger.warn("Could not get thread context");
		}
	}
	
	public MonitoringEvent(FlowElement flowElement, MonitoringEventType type, VariableStack stack) {
		this(EventClass.MONITORING_EVENT, flowElement, type, stack);
	}

	public ThreadingContext getThread() {
		return thread;
	}

	public MonitoringEventType getType() {
		return type;
	}

	public VariableStack getStack() {
		return stack;
	}
	
	
}