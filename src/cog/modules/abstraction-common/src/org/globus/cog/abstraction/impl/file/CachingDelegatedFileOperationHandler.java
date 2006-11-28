// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2004
 */
package org.globus.cog.abstraction.impl.file;

import java.io.IOException;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class CachingDelegatedFileOperationHandler extends TaskHandlerImpl {
	private FileResource resource;

	public synchronized void submit(Task task) throws IllegalSpecException,
			InvalidSecurityContextException, InvalidServiceContactException,
			TaskSubmissionException {
		Service service = task.getService(0);
		if (service == null) {
			setTaskStatus(task, Status.FAILED, null, "Service is not set");
			throw new IllegalSpecException("Service is not set");
		}
		try {
			super.submit(task, getResource(service));
			setTaskStatus(task, Status.COMPLETED, null, null);
		}
		catch (TaskSubmissionException e) {
			setTaskStatus(task, Status.FAILED, e, e.getMessage());
			throw e;
		}
		catch (IllegalSpecException e) {
			setTaskStatus(task, Status.FAILED, e, e.getMessage());
			throw e;
		}
		catch (Exception e) {
			setTaskStatus(task, Status.FAILED, e, e.getMessage());
			throw new TaskSubmissionException(e.getMessage(), e);
		}
		finally {
			stopResources();
		}
	}

	protected void setTaskStatus(Task task, int statusCode, Exception exception, String message) {
		Status status = new StatusImpl();
		status.setStatusCode(statusCode);
		status.setException(exception);
		status.setMessage(message);
		task.setStatus(status);
	}

	protected FileResource getResource(Service service) throws InvalidProviderException,
			ProviderMethodException, IllegalHostException, InvalidSecurityContextException,
			FileResourceException, IOException {
		resource = FileResourceCache.getDefault().getResource(service);
		return resource;
	}

	public void stopResources() {
		if (resource == null) {
			return;
		}
		FileResourceCache.getDefault().releaseResource(resource);
		resource = null;
	}

	protected Object execute(FileResource fileResource, FileOperationSpecification spec)
			throws FileResourceException, IOException {
		Object ret;
		try {
			ret = super.execute(fileResource, spec);
		}
		finally {
			stopResources();
		}
		return ret;
	}

	protected FileResource getResource() {
		return resource;
	}
}