// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.slocal.file;

import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.slocal.Util;
import org.globus.cog.abstraction.interfaces.Task;

/**
 * Extends the base class TaskHandlerImpl in org.globus.cog.core.impl.file
 * 
 * @author nvijayak
 * 
 */
public class TaskHandlerImpl extends org.globus.cog.abstraction.impl.file.TaskHandlerImpl {
	public synchronized void submit(Task task) throws IllegalSpecException,
			InvalidSecurityContextException, InvalidServiceContactException,
			TaskSubmissionException {
		throw new TaskSubmissionException("Not yet implemented");
		//Util.checkCredentials(task);
		//super.submit(task);
	}
}
