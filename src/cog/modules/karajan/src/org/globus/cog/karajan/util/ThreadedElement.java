// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 10, 2003
 */
package org.globus.cog.karajan.util;

import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class ThreadedElement {
	private FlowElement element;

	private ThreadingContext thread;

	public ThreadedElement(FlowElement element, ThreadingContext thread) {
		this.element = element;
		this.thread = thread;
	}

	public FlowElement getElement() {
		return element;
	}

	public ThreadingContext getThread() {
		return thread;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ThreadedElement)) {
			return false;
		}
		return element.equals(((ThreadedElement) o).getElement())
				&& thread.equals(((ThreadedElement) o).getThread());
	}

	public int hashCode() {
		return element.hashCode() + thread.hashCode();
	}
	
	public String toString() {
		return element.toString()+" - "+thread.toString();
	}

}