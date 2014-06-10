// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.sshcl.execution;

import org.globus.cog.abstraction.impl.common.AbstractTaskHandler;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;

/**
 * Provides SSH specific <code>TaskHandler</code> for job submission task and
 * file transfer task.
 * 
 * @author Kaizar Amin (amin@mcs.anl.gov)
 */
public class TaskHandlerImpl extends
		AbstractTaskHandler {

	protected DelegatedTaskHandler newDelegatedTaskHandler(int type) {
		return new JobSubmissionTaskHandler();
	}

	public String getName() {
		return "SSH";
	}
}