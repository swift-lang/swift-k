// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.invocation.ws;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class InvocationTaskHandler implements DelegatedTaskHandler {
    private static Logger logger = Logger
            .getLogger(InvocationTaskHandler.class);

    private Task task = null;

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        if (this.task != null) {
            throw new TaskSubmissionException(
                    "InvocationTaskHandler cannot handle two active tasks simultaneously");
        } else {
            this.task = task;
            InvocationThread thread = new InvocationThread(this.task);
            try {
                // check if the task has not been canceled after it was
                // submitted for execution
                if (this.task.getStatus().getStatusCode() == Status.UNSUBMITTED) {
                    this.task.setStatus(Status.SUBMITTED);
                    thread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Status newStatus = new StatusImpl();
                Status oldStatus = this.task.getStatus();
                newStatus.setPrevStatusCode(oldStatus.getStatusCode());
                newStatus.setStatusCode(Status.FAILED);
                newStatus.setException(e);
                this.task.setStatus(newStatus);
            }

        }
    }

    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public void cancel() throws InvalidSecurityContextException,
            TaskSubmissionException {

        this.task.setStatus(Status.CANCELED);
    }
}