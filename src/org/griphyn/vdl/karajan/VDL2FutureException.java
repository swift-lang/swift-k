/*
 * Created on Jan 4, 2007
 */
package org.griphyn.vdl.karajan;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.griphyn.vdl.mapping.DSHandle;

public class VDL2FutureException extends RuntimeException {
	private final DSHandle handle;
	private FlowElement listener;
	private VariableStack stack;

	public VDL2FutureException(DSHandle handle, FlowElement listener, VariableStack stack) {
		this.handle = handle;
		this.listener = listener;
		this.stack = stack;
	}

	public VDL2FutureException(DSHandle handle) {
		this(handle, null, null);
	}

	public DSHandle getHandle() {
		return handle;
	}

	public VariableStack getStack() {
		return stack;
	}

	public FlowElement getListener() {
		return listener;
	}

	public void setStack(VariableStack stack) {
		this.stack = stack;
	}

	public void setListener(FlowElement listener) {
		this.listener = listener;
	}
}
