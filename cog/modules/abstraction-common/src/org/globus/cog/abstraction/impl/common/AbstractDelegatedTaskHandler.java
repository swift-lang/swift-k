//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 4, 2009
 */
package org.globus.cog.abstraction.impl.common;

import java.util.Map;

import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public abstract class AbstractDelegatedTaskHandler implements
        DelegatedTaskHandler {
    private Task task;
    private Map<String, Object> attributes;
    
    public void cancel() throws InvalidSecurityContextException,
            TaskSubmissionException {
        cancel("Canceled");
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
    
    protected void checkAndSetTask(Task task) throws TaskSubmissionException {
        if (this.task != null) {
            throw new TaskSubmissionException(
                    "This task handler cannot handle two active tasks simultaneously");
        }
        else if (task.getStatus().getStatusCode() != Status.UNSUBMITTED) {
            throw new TaskSubmissionException(
                    "Task is not in unsubmitted state");
        }
        setTask(task);
    }
    
    protected void failTask(String message, Exception exception) {
        Status newStatus = new StatusImpl();
        Status oldStatus = getTask().getStatus();
        newStatus.setPrevStatusCode(oldStatus.getStatusCode());
        newStatus.setStatusCode(Status.FAILED);
        newStatus.setMessage(message);
        newStatus.setException(exception);
        getTask().setStatus(newStatus);
    }

    @Override
    public void setAttributes(Map<String, Object> attributes) {
    	this.attributes = attributes;
    }
}
