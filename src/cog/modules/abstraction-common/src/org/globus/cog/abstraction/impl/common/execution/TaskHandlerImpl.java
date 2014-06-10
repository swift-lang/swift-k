// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.execution;

import org.globus.cog.abstraction.impl.common.AbstractTaskHandler;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.Task;

/**
 * Provides generic <code>TaskHandler</code> s for job submission task.
 */
public abstract class TaskHandlerImpl extends AbstractTaskHandler {

	protected final DelegatedTaskHandler newDelegatedTaskHandler(int type)
			throws TaskSubmissionException {
		if (type != Task.JOB_SUBMISSION) {
			throw new TaskSubmissionException(getName()
					+ " execution task handler can only handle job submission tasks");
		}
		return newDelegatedTaskHandler();
	}

	protected abstract DelegatedTaskHandler newDelegatedTaskHandler();
	
	public String toString() {
        return "ExecutionTaskHandler(provider = " + getName() + ")";
    }
}
