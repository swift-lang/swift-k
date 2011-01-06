//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 18, 2005
 */
package org.globus.cog.karajan.viewer;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.events.ControlEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class ThreadedUID {
	private Integer id;
	private ThreadingContext thread;
	private FlowElement element;
	
	public ThreadedUID(NotificationEvent e) throws VariableNotFoundException {
		this(e.getFlowElement(), e.getStack());
	}

	public ThreadedUID(FlowElement fe, ControlEvent e) throws VariableNotFoundException {
		this(fe, e.getStack());
	}
	
	public ThreadedUID(FlowElement fe, VariableStack stack) throws VariableNotFoundException {
		this(fe, ThreadingContext.get(stack));
	}

	public ThreadedUID(FlowElement fe, ThreadingContext tc) {
		id = (Integer) fe.getProperty(FlowElement.UID);
		this.thread = tc;
		this.element = fe;
	}

	public ThreadedUID(Integer id, ThreadingContext tc) {
		this.id = id;
		this.thread = tc;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ThreadedUID) {
			ThreadedUID tu = (ThreadedUID) obj;
			return id.equals(tu.id) && thread.equals(tu.thread);
		}
		return false;
	}

	public int hashCode() {
		return thread.hashCode() + id.hashCode();
	}

	protected FlowElement getElement() {
		return element;
	}

	protected void setElement(FlowElement element) {
		this.element = element;
	}

	protected ThreadingContext getThread() {
		return thread;
	}

}
