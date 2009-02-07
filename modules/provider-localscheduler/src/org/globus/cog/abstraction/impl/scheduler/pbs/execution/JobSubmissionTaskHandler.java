//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.scheduler.pbs.execution;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.impl.scheduler.pbs.PBSExecutor;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class JobSubmissionTaskHandler extends AbstractDelegatedTaskHandler implements 
        ProcessListener {

    private static Logger logger = Logger
            .getLogger(JobSubmissionTaskHandler.class);

    private JobSpecification spec;
    private Thread thread;
    private PBSExecutor executor;

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        checkAndSetTask(task);
        task.setStatus(Status.SUBMITTING);
        try {
            spec = (JobSpecification) task.getSpecification();
        }
        catch (Exception e) {
            throw new IllegalSpecException(
                    "Exception while retreiving Job Specification", e);
        }
        if (task.getAllServices() == null
                || task.getAllServices().size() == 0
                || task.getService(0) == null) {
            throw new InvalidSecurityContextException(
                    "No service specified");
        }

        try {
            synchronized(this) {
                if (task.getStatus().getStatusCode() != Status.CANCELED) {
                    executor = new PBSExecutor(task, this);
                    executor.start();
                    task.setStatus(Status.SUBMITTED);
                    if (spec.isBatchJob()) {
                        task.setStatus(Status.COMPLETED);
                    }
                }
            }
        }
        catch (Exception e) {
            if (e.getMessage() != null) {
                throw new TaskSubmissionException("Cannot submit job: "
                        + e.getMessage(), e);
            }
            else {
                throw new TaskSubmissionException("Cannot submit job", e);
            }
        }
    }

    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public synchronized void cancel(String message) throws InvalidSecurityContextException,
            TaskSubmissionException {
        executor.cancel();
        getTask().setStatus(new StatusImpl(Status.CANCELED, message, null));
    }

    public void processCompleted(int exitCode) {
        if (getTask().getStatus().getStatusCode() != Status.FAILED) {
            if (exitCode == 0) {
                getTask().setStatus(Status.COMPLETED);
            }
            else {
                failTask(null, new JobException(exitCode));
            }
        }
    }

    public void processFailed(String message) {
        failTask(message, null);
    }

    public void processFailed(Exception e) {
        failTask(null, e);
    }

    public void statusChanged(int status) {
        if (status == Job.STATE_RUNNING) {
            getTask().setStatus(Status.ACTIVE);
        }
    }

    public void stderrUpdated(String stderr) {
        if (FileLocation.MEMORY.overlaps(spec.getStdErrorLocation())) {
            getTask().setStdError(stderr);
        }
    }

    public void stdoutUpdated(String stdout) {
        if (FileLocation.MEMORY.overlaps(spec.getStdOutputLocation())) {
            getTask().setStdOutput(stdout);
        }
    }
}
