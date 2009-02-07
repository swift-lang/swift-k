// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.sandbox;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.TaskHandlerSkeleton;
import org.globus.cog.abstraction.impl.common.task.ActiveTaskException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class SandboxingTaskHandler extends TaskHandlerSkeleton {
	private static Logger logger = Logger.getLogger(SandboxingTaskHandler.class);
	private static Class[] taskArg = { Task.class };
	private static Class[] taskAndMsgArg = { Task.class, String.class };

	private TaskHandler handler;
	private Sandbox sandbox;

	public SandboxingTaskHandler(Sandbox sandbox, TaskHandler handler) {
		this.handler = handler;
		this.sandbox = sandbox;
	}

	public void submit(Task task) throws IllegalSpecException, InvalidSecurityContextException,
			InvalidServiceContactException, TaskSubmissionException {
		try {
			sandbox.invoke(handler, "submit", taskArg, new Object[] { task });
		}
		catch (IllegalSpecException e) {
			throw e;
		}
		catch (InvalidSecurityContextException e) {
			throw e;
		}
		catch (InvalidServiceContactException e) {
			throw e;
		}
		catch (TaskSubmissionException e) {
			throw e;
		}
		catch (Throwable e) {
			logger.debug("Unexpected exception", e);
			throw new SandboxException("Unexpected exception: "+e.getMessage(), e);
		}
	}

	public void suspend(Task task) throws InvalidSecurityContextException, TaskSubmissionException {
		try {
			sandbox.invoke(handler, "suspend", taskArg, new Object[] { task });
		}
		catch (InvalidSecurityContextException e) {
			throw e;
		}
		catch (TaskSubmissionException e) {
			throw e;
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}

	public void resume(Task task) throws InvalidSecurityContextException, TaskSubmissionException {
		try {
			sandbox.invoke(handler, "resume", taskArg, new Object[] { task });
		}
		catch (InvalidSecurityContextException e) {
			throw e;
		}
		catch (TaskSubmissionException e) {
			throw e;
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}
	
	public void cancel(Task task, String message) throws InvalidSecurityContextException, TaskSubmissionException {
		try {
			sandbox.invoke(handler, "cancel", taskAndMsgArg, new Object[] { task, message });
		}
		catch (InvalidSecurityContextException e) {
			throw e;
		}
		catch (TaskSubmissionException e) {
			throw e;
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}

	public void remove(Task task) throws ActiveTaskException {
		try {
			sandbox.invoke(handler, "remove", taskArg, new Object[] { task });
		}
		catch (ActiveTaskException e) {
			throw e;
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}

	public Collection getAllTasks() {
		try {
			return (Collection) sandbox.invoke(handler, "getAllTasks", null, null);
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}

	public Collection getActiveTasks() {
		try {
			return (Collection) sandbox.invoke(handler, "getActiveTasks", null, null);
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}

	public Collection getCanceledTasks() {
		try {
			return (Collection) sandbox.invoke(handler, "getCanceledTasks", null, null);
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}

	public Collection getCompletedTasks() {
		try {
			return (Collection) sandbox.invoke(handler, "getCompletedTasks", null, null);
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}

	public Collection getFailedTasks() {
		try {
			return (Collection) sandbox.invoke(handler, "getFailedTasks", null, null);
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}

	public Collection getResumedTasks() {
		try {
			return (Collection) sandbox.invoke(handler, "getResumedTasks", null, null);
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}

	public Collection getSuspendedTasks() {
		try {
			return (Collection) sandbox.invoke(handler, "getSuspendedTasks", null, null);
		}
		catch (Throwable e) {
			throw new SandboxException("Unexpected exception", e);
		}
	}
}