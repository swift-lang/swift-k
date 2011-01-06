// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 25, 2005
 */
package org.globus.cog.karajan.workflow.events;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class AbortEvent extends ControlEvent {
	public static final ControlEventType ABORT = new ControlEventType("ABORT", 4);

	private final ThreadingContext context;

	public AbortEvent(FlowElement flowElement, ThreadingContext context, VariableStack stack) {
		super(flowElement, ABORT, stack);
		this.context = context;
	}

	public ThreadingContext getContext() {
		return context;
	}
}