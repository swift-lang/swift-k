//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 13, 2012
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import k.rt.FutureObject;
import k.rt.Stack;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;

public class TaskStateFuture extends FutureObject implements StatusListener {
	private final Stack stack;
	private final Task task;
	
	public TaskStateFuture(Stack stack, Task task) {
		this.stack = stack;
		this.task = task;
	}

	public Stack getStack() {
		return stack;
	}
	
	public Task getTask() {
		return task;
	}

	@Override
	public void statusChanged(StatusEvent event) {
		Status s = event.getStatus();
		int code = s.getStatusCode();
		if (code == Status.COMPLETED) {
			resume();
		}
		else if (code == Status.FAILED) {
			fail(new RuntimeException(s.getException()));
		}
	}
	
	protected void resume() {
		setValue(Boolean.TRUE);
	}
}
