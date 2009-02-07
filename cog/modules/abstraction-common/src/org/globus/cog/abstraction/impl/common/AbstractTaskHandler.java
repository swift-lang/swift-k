// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
	private Map handleMap;

	public AbstractTaskHandler() {
		this.handleMap = new HashMap();
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

	protected abstract String getName();

	public void suspend(Task task) throws InvalidSecurityContextException, TaskSubmissionException {
		DelegatedTaskHandler dth = (DelegatedTaskHandler) this.handleMap.get(task);
		if (dth != null) {
			dth.suspend();
		}
	}

	public void resume(Task task) throws InvalidSecurityContextException, TaskSubmissionException {
		DelegatedTaskHandler dth = (DelegatedTaskHandler) this.handleMap.get(task);
		if (dth != null) {
			dth.resume();
		}
	}
	
	public void cancel(Task task, String message) throws InvalidSecurityContextException, TaskSubmissionException {
		DelegatedTaskHandler dth = (DelegatedTaskHandler) this.handleMap.get(task);
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
	public Collection getAllTasks() {
		try {
			return new ArrayList(handleMap.keySet());
		}
		catch (Exception e) {
			return null;
		}
	}
	
	protected Collection getTasksWithStatus(int code) {
        ArrayList l = new ArrayList();
        synchronized(handleMap) {
            Iterator i = handleMap.keySet().iterator();
            while (i.hasNext()) {
                Task t = (Task) i.next();
                if (t.getStatus().getStatusCode() == code) {
                	l.add(t);
                }
            }
        }
        return l;
    }

	/** return a collection of active tasks */
	public Collection getActiveTasks() {
	    return getTasksWithStatus(Status.ACTIVE);
	}

	/** return a collection of failed tasks */
	public Collection getFailedTasks() {
	    return getTasksWithStatus(Status.FAILED);
	}

	/** return a collection of completed tasks */
	public Collection getCompletedTasks() {
	    return getTasksWithStatus(Status.COMPLETED);
	}

	/** return a collection of suspended tasks */
	public Collection getSuspendedTasks() {
	    return getTasksWithStatus(Status.SUSPENDED);
	}

	/** return a collection of resumed tasks */
	public Collection getResumedTasks() {
	    return getTasksWithStatus(Status.RESUMED);
	}

	/** return a collection of canceled tasks */
	public Collection getCanceledTasks() {
	    return getTasksWithStatus(Status.CANCELED);
	}
}
