// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.events;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public abstract class FlowEvent extends Event {

	private VariableStack stack;

	public FlowEvent(EventClass cls, FlowElement flowElement, VariableStack stack) {
		super(cls, flowElement, Priority.NORMAL);
		this.stack = stack;
	}

	public FlowEvent(EventClass cls, FlowElement flowElement, VariableStack stack, Priority priority) {
		super(cls, flowElement, priority);
		this.stack = stack;
	}

	public VariableStack getStack() {
		return stack;
	}

	public void setStack(VariableStack stack) {
		this.stack = stack;
	}

	public boolean hasStack() {
		return stack != null;
	}

	protected String getThreadingContext() {
		if (stack != null) {
			try {
				return ThreadingContext.get(stack).toString();
			}
			catch (VariableNotFoundException e) {
				return "UNKNOWN THREAD";
			}
		}
		return "";
	}
}
