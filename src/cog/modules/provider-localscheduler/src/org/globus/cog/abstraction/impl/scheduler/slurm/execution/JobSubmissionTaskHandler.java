package org.globus.cog.abstraction.impl.scheduler.slurm.execution;

import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractJobSubmissionTaskHandler;
import org.globus.cog.abstraction.impl.scheduler.slurm.SlurmExecutor;
import org.globus.cog.abstraction.interfaces.Task;

public class JobSubmissionTaskHandler extends AbstractJobSubmissionTaskHandler {
	protected AbstractExecutor newExecutor(Task task,
			AbstractJobSubmissionTaskHandler th) {
		return new SlurmExecutor(task, th);
	}
}