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
        task.setStatus(Status.SUBMITTING);
        Service service = task.getService(0);
        if (service == null) {
            throw new IllegalSpecException("Service is not set");
        }
        FileResource fr = null;
        try {
            fr = getResource(service);
            super.submit(task, fr);
            setTaskStatus(task, Status.COMPLETED, null, null);
        }
        catch (TaskSubmissionException e) {
            throw e;
        }
        catch (IllegalSpecException e) {
            throw e;
        }
        catch (IrrecoverableResourceException e) {
            FileResourceCache.getDefault().invalidateResource(resource);
            throw new TaskSubmissionException(e);
        }
        catch (Exception e) {
            throw new TaskSubmissionException(e);
        }
        finally {
            stopResources();
        }
    }

    protected void setTaskStatus(Task task, int statusCode,
            Exception exception, String message) {
        Status status = new StatusImpl();
        status.setStatusCode(statusCode);
        status.setException(exception);
        status.setMessage(message);
        task.setStatus(status);
    }

    protected synchronized FileResource getResource(Service service)
            throws InvalidProviderException, ProviderMethodException,
            IllegalHostException, InvalidSecurityContextException,
            FileResourceException, IOException {
        resource = FileResourceCache.getDefault().getResource(service);
        return resource;
    }

    public synchronized void stopResources() {
        if (resource == null) {
            return;
        }
        FileResourceCache.getDefault().releaseResource(resource);
        resource = null;
    }

    protected Object execute(FileResource fileResource,
            FileOperationSpecification spec) throws FileResourceException,
            IOException {
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