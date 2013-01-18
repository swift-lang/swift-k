package org.globus.cog.abstraction.impl.scheduler.lsf.execution;
import org.globus.cog.abstraction.impl.scheduler.lsf.LSFExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractJobSubmissionTaskHandler;
import org.globus.cog.abstraction.interfaces.Task;

public class JobSubmissionTaskHandler extends AbstractJobSubmissionTaskHandler {
	protected AbstractExecutor newExecutor(Task task,
			AbstractJobSubmissionTaskHandler th) {
		return new LSFExecutor(task, th);
	}
}