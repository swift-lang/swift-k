// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.mpichg2;

import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;

/**
 * Provides MpichG2 specific <code>TaskHandler</code> s for job submission
 * task.
 */
public class TaskHandlerImpl extends
        org.globus.cog.abstraction.impl.common.execution.TaskHandlerImpl {

    protected DelegatedTaskHandler newDelegatedTaskHandler() {
        return new JobSubmissionTaskHandler();
    }

    protected String getName() {
        return "MPICHG2";
    }
}
