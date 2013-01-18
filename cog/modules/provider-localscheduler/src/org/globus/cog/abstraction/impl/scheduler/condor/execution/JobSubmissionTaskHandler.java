//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.scheduler.condor.execution;

import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractJobSubmissionTaskHandler;
import org.globus.cog.abstraction.impl.scheduler.condor.CondorExecutor;
import org.globus.cog.abstraction.interfaces.Task;

public class JobSubmissionTaskHandler extends AbstractJobSubmissionTaskHandler {
	protected AbstractExecutor newExecutor(Task task,
			AbstractJobSubmissionTaskHandler th) {
		return new CondorExecutor(task, th);
	}
}