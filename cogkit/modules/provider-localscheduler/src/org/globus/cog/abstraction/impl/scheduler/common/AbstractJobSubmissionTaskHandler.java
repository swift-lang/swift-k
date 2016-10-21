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

package org.globus.cog.abstraction.impl.scheduler.common;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public abstract class AbstractJobSubmissionTaskHandler extends
		AbstractDelegatedTaskHandler implements ProcessListener {

	private static Logger logger = Logger
			.getLogger(AbstractJobSubmissionTaskHandler.class);

	private JobSpecification spec;
	private AbstractExecutor executor;

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
		if (task.getAllServices() == null || task.getAllServices().size() == 0
				|| task.getService(0) == null) {
			throw new InvalidSecurityContextException("No service specified");
		}

		try {
			synchronized (this) {
				if (task.getStatus().getStatusCode() != Status.CANCELED) {
					executor = newExecutor(task, this);
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
				throw new TaskSubmissionException(e.getMessage(), e);
			}
			else {
				throw new TaskSubmissionException("Cannot submit job", e);
			}
		}
	}

	protected abstract AbstractExecutor newExecutor(Task task,
			AbstractJobSubmissionTaskHandler th);

	public void suspend() throws InvalidSecurityContextException,
			TaskSubmissionException {
	}

	public void resume() throws InvalidSecurityContextException,
			TaskSubmissionException {
	}

	public synchronized void cancel(String message)
			throws InvalidSecurityContextException, TaskSubmissionException {
		executor.cancel();
		getTask().setStatus(new StatusImpl(Status.CANCELED, message, null));
	}

	public void processCompleted(int exitCode) {
	    if (logger.isDebugEnabled()) {
	        logger.debug("Process " + getTask().getIdentity() + " completed. Exit code: " + exitCode);
	    }
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
	    if (logger.isDebugEnabled()) {
            logger.debug("Process " + getTask().getIdentity() + " failed: " + message);
        }
		failTask(message, null);
	}

	public void processFailed(Exception e) {
	    if (logger.isDebugEnabled()) {
            logger.debug("Process " + getTask().getIdentity() + " failed: " + e);
        }
		failTask(null, e);
	}

	public void statusChanged(int status) {
	    if (logger.isDebugEnabled()) {
            logger.debug("Process " + getTask().getIdentity() + " status changed: " + status);
        }
		if (status == Job.STATE_RUNNING) {
		    Job job = executor.getJob();
		    String location = null;
		    if (job != null) {
		        location = executor.getJob().getLocation();
		    }
		    Status newStatus = new StatusImpl();
		    Status prev = getTask().getStatus();
		    newStatus.setPrevStatusCode(prev.getStatusCode());
		    newStatus.setStatusCode(Status.ACTIVE);
		    if (location != null) {
		        newStatus.setMessage("location=" + location);
		    }
			getTask().setStatus(newStatus);
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
