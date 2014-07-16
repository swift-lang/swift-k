package org.globus.cog.abstraction.impl.scheduler.slurm.execution;

import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;

/**
 *Provides a local PBS <code>TaskHandler</code>
 *for job submission to the local resource without
 *any security context.
 *
 */
public class TaskHandlerImpl extends
		org.globus.cog.abstraction.impl.common.execution.TaskHandlerImpl {

	protected DelegatedTaskHandler newDelegatedTaskHandler() {
		return new JobSubmissionTaskHandler();
	}

	public String getName() {
		return "Slurm";
	}
}