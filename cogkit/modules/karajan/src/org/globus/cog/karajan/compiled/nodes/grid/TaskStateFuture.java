/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
