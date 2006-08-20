// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 22, 2004
 */
package org.globus.cog.karajan.workflow.events;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public final class StatusMonitoringEvent extends MonitoringEvent {

	public static final MonitoringEventType EXECUTION_STARTED = new MonitoringEventType("EXECUTION_STARTED", 0);
	public static final MonitoringEventType EXECUTION_COMPLETED = new MonitoringEventType("EXECUTION_COMPLETED", 1);
	public static final MonitoringEventType EXECUTION_FAILED = new MonitoringEventType("EXECUTION_FAILED", 2);
	public static final MonitoringEventType EXECUTION_ABORTED = new MonitoringEventType("EXECUTION_BORTED", 3);

	private final String message;

	public StatusMonitoringEvent(FlowElement flowElement, MonitoringEventType type, VariableStack stack,
			String message) {
		super(flowElement, type, stack);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public String toString() {
		if (message == null) {
			return "StatusMonitoringEvent: " + getFlowElement() + " - " + getType();
		}
		else {
			return "StatusMonitoringEvent: " + getFlowElement() + " - " + getType() + " ("
					+ message + ")";
		}
	}
}