//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.slocal.execution;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.slocal.Executor;
import org.globus.cog.abstraction.impl.slocal.ProcessListener;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

/**
 * @author Kaizar Amin (amin@mcs.anl.gov)
 *
 */
public class JobSubmissionTaskHandler extends AbstractDelegatedTaskHandler implements ProcessListener {

	private static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class);

	private Thread thread = null;

	public void submit(Task task) throws IllegalSpecException, InvalidSecurityContextException,
			InvalidServiceContactException, TaskSubmissionException {
		checkAndSetTask(task);
		JobSpecification spec;
		try {
			spec = (JobSpecification) task.getSpecification();
		}
		catch (Exception e) {
			throw new IllegalSpecException("Exception while retreiving Job Specification", e);
		}

		if (task.getAllServices() == null || task.getAllServices().size() == 0
				|| task.getService(0) == null) {
			throw new InvalidSecurityContextException("No service specified");
		}
		if (task.getService(0).getSecurityContext() == null
				|| task.getService(0).getSecurityContext().getCredentials() == null) {
			throw new InvalidSecurityContextException("No credentials supplied");
		}

		try {
			new Executor(task, task.getService(0).getSecurityContext().getCredentials(), this).start();
			// check if the task has not been canceled after it was
			// submitted for execution
			if (task.getStatus().getStatusCode() == Status.UNSUBMITTED) {
				task.setStatus(Status.SUBMITTED);
				if (spec.isBatchJob()) {
					task.setStatus(Status.COMPLETED);
				}
			}
		}
		catch (Exception e) {
			if (e.getMessage() != null) {
				throw new TaskSubmissionException("Cannot submit job: " + e.getMessage(), e);
			}
			else {
				throw new TaskSubmissionException("Cannot submit job", e);
			}
		}
	}

	public void suspend() throws InvalidSecurityContextException, TaskSubmissionException {
	}

	public void resume() throws InvalidSecurityContextException, TaskSubmissionException {
	}

	public void cancel(String message) throws InvalidSecurityContextException, TaskSubmissionException {
		getTask().setStatus(new StatusImpl(Status.CANCELED, message, null));
	}

	public void processCompleted(int exitCode) {
		if (getTask().getStatus().getStatusCode() != Status.FAILED) {
			if (exitCode == 0) {
				getTask().setStatus(Status.COMPLETED);
			}
			else {
			    failTask("Process failed with exit code " + exitCode, null);
			}
		}
	}

	public void processFailed(String message) {
	    failTask(message, null);
	}
}