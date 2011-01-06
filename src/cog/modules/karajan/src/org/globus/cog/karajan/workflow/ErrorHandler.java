// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 16, 2004
 */
package org.globus.cog.karajan.workflow;

import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.nodes.ErrorHandlerNode;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class ErrorHandler {
	private String type;

	private FlowElement handler;

	public ErrorHandler() {
	}

	public ErrorHandler(String type, FlowElement handler) {
		this.type = type;
		this.handler = handler;
	}

	public FlowElement getHandler() {
		return handler;
	}

	public String getType() {
		return type;
	}

	public boolean matches(String error) {
		return error.matches(type);
	}

	public void handleError(FlowElement source, FailureNotificationEvent error)
			throws ExecutionException {
		((ErrorHandlerNode) handler).handleError(source, error);
	}

	public void setHandler(FlowElement handler) {
		this.handler = handler;
	}

	public void setType(String type) {
		this.type = type;
	}
}