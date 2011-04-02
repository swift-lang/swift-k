// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 23, 2004
 */
package org.globus.cog.karajan.workflow.events;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class ProgressMonitoringEvent extends MonitoringEvent {
	public static final MonitoringEventType LOOP_PROGRESS = new MonitoringEventType("LOOP_PROGRESS", 4);
	private final long total, current;

	public ProgressMonitoringEvent(FlowElement flowElement, MonitoringEventType type, VariableStack stack,
			long total, long current) {
		super(flowElement, type, stack);
		this.total = total;
		this.current = current;
	}

	public long getCurrent() {
		return current;
	}

	public long getTotal() {
		return total;
	}
}