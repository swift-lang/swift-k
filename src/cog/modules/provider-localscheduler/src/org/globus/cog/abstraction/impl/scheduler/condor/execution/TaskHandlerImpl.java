
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.scheduler.condor.execution;

import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;

/**
 *Provides a local Condor <code>TaskHandler</code>
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
		return "Condor";
	}
}
