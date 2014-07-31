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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.globus.cog.abstraction.impl.common.task.ActiveTaskException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

/**
 * Provides an abstract <code>TaskHandler</code>.
 */
public abstract class AbstractTaskHandler extends TaskHandlerSkeleton {
	private Map<Task, DelegatedTaskHandler> handleMap;

	public AbstractTaskHandler() {
		this.handleMap = new HashMap<Task, DelegatedTaskHandler>();
		setType(TaskHandler.GENERIC);
	}

	public void submit(Task task) throws IllegalSpecException, InvalidSecurityContextException,
			InvalidServiceContactException, TaskSubmissionException {
		if (task.getStatus().getStatusCode() != Status.UNSUBMITTED) {
			throw new TaskSubmissionException("TaskHandler can only handle unsubmitted tasks");
		}
		int type = task.getType();
		DelegatedTaskHandler dth = newDelegatedTaskHandler(type);
		synchronized(this.handleMap) {
		    this.handleMap.put(task, dth);
		}
		dth.submit(task);
	}

	protected abstract DelegatedTaskHandler newDelegatedTaskHandler(int type) throws TaskSubmissionException;

	public abstract String getName();
	
	public void setName(String name) {
	    // ignored for most handlers
	}

	public void suspend(Task task) throws InvalidSecurityContextException, TaskSubmissionException {
		DelegatedTaskHandler dth = this.handleMap.get(task);
		if (dth != null) {
			dth.suspend();
		}
	}

	public void resume(Task task) throws InvalidSecurityContextException, TaskSubmissionException {
		DelegatedTaskHandler dth = this.handleMap.get(task);
		if (dth != null) {
			dth.resume();
		}
	}
	
	public void cancel(Task task, String message) throws InvalidSecurityContextException, TaskSubmissionException {
		DelegatedTaskHandler dth = this.handleMap.get(task);
		if (dth != null) {
			dth.cancel(message);
		}
		else {
			task.setStatus(new StatusImpl(Status.CANCELED, message, null));
		}
	}

	public void remove(Task task) throws ActiveTaskException {
		if (!handleMap.containsKey(task)) {
			return;
		}
		int status = task.getStatus().getStatusCode();
		if ((status == Status.ACTIVE)) {
			throw new ActiveTaskException("Cannot remove an active or suspended Task");
		}
		else {
		    synchronized(this.handleMap) {
		        this.handleMap.remove(task);
		    }
		}
	}

	/** return a collection of all tasks submitted to the handler */
	public Collection<Task> getAllTasks() {
		try {
			return new ArrayList<Task>(handleMap.keySet());
		}
		catch (Exception e) {
			return null;
		}
	}
	
	protected Collection<Task> getTasksWithStatus(int code) {
        Collection<Task> list = new ArrayList<Task>();
        synchronized(handleMap) {
            for (Task task : handleMap.keySet()) {
                if (task.getStatus().getStatusCode() == code) {
                	list.add(task);
                }
            }
        }
        return list;
    }

	/** return a collection of active tasks */
	public Collection<Task> getActiveTasks() {
	    return getTasksWithStatus(Status.ACTIVE);
	}

	/** return a collection of failed tasks */
	public Collection<Task> getFailedTasks() {
	    return getTasksWithStatus(Status.FAILED);
	}

	/** return a collection of completed tasks */
	public Collection<Task> getCompletedTasks() {
	    return getTasksWithStatus(Status.COMPLETED);
	}

	/** return a collection of suspended tasks */
	public Collection<Task> getSuspendedTasks() {
	    return getTasksWithStatus(Status.SUSPENDED);
	}

	/** return a collection of resumed tasks */
	public Collection<Task> getResumedTasks() {
	    return getTasksWithStatus(Status.RESUMED);
	}

	/** return a collection of canceled tasks */
	public Collection<Task> getCanceledTasks() {
	    return getTasksWithStatus(Status.CANCELED);
	}
}
