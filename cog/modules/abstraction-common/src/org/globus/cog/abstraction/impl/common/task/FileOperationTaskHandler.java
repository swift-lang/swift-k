// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class FileOperationTaskHandler implements TaskHandler {
    private Hashtable mapping;
    private int type;

    public FileOperationTaskHandler() {
        this.mapping = new Hashtable();
        this.type = TaskHandler.FILE_OPERATION;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        if (task.getType() != Task.FILE_OPERATION) {
            throw new TaskSubmissionException(
                    "File operation handler can only handle file operation tasks");
        }
        String provider = task.getService(0).getProvider().toLowerCase();
        TaskHandler taskHandler = (TaskHandler) this.mapping.get(provider);
        if (taskHandler == null) {
            try {
                taskHandler = createTaskHandler(provider);
            } catch (InvalidProviderException ipe) {
                throw new TaskSubmissionException("Cannot submit task", ipe);
            }
        }
                 
        taskHandler.submit(task);
    }

    public void suspend(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        if (task.getType() != Task.FILE_OPERATION) {
            throw new TaskSubmissionException(
                    "File operation handler can only handle file operation tasks");
        }
        String provider = task.getService(0).getProvider().toLowerCase();
        TaskHandler taskHandler = (TaskHandler) this.mapping.get(provider);
        if (taskHandler != null) {
            taskHandler.suspend(task);
        } else {
            throw new TaskSubmissionException("Provider " + provider
                    + " unknown");
        }
    }

    public void resume(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        if (task.getType() != Task.FILE_OPERATION) {
            throw new TaskSubmissionException(
                    "File operation handler can only handle file operation tasks");
        }
        String provider = task.getService(0).getProvider().toLowerCase();
        TaskHandler taskHandler = (TaskHandler) this.mapping.get(provider);
        if (taskHandler != null) {
            taskHandler.resume(task);
        } else {
            throw new TaskSubmissionException("Provider " + provider
                    + " unknown");
        }
    }

    public void cancel(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        if (task.getType() != Task.FILE_OPERATION) {
            throw new TaskSubmissionException(
                    "File operation handler can only handle file operation tasks");
        }
        String provider = task.getService(0).getProvider().toLowerCase();
        TaskHandler taskHandler = (TaskHandler) this.mapping.get(provider);
        if (taskHandler != null) {
            taskHandler.cancel(task);
        } else {
            task.setStatus(Status.CANCELED);
        }
    }

    public void remove(Task task) throws ActiveTaskException {
    	String provider = task.getService(0).getProvider().toLowerCase();
        TaskHandler taskHandler = (TaskHandler) this.mapping.get(provider);
        if (taskHandler != null) {
            taskHandler.remove(task);
        }
    }

    public Collection getAllTasks() {
        // extract all the tasks from various TaskHandlers
        List list = new ArrayList();
        Enumeration e1 = this.mapping.elements();
        TaskHandler handler;
        while (e1.hasMoreElements()) {
            handler = (TaskHandler) e1.nextElement();
            list.addAll(handler.getAllTasks());
        }
        return list;
    }

    public Collection getActiveTasks() {
        // extract all the active tasks from various TaskHandlers
        List list = new ArrayList();
        Enumeration e1 = this.mapping.elements();
        TaskHandler handler;
        while (e1.hasMoreElements()) {
            handler = (TaskHandler) e1.nextElement();
            list.addAll(handler.getAllTasks());
        }
        return list;
    }

    public Collection getFailedTasks() {
        // extract all the failed tasks from various TaskHandlers
        List list = new ArrayList();
        Enumeration e1 = this.mapping.elements();
        TaskHandler handler;
        while (e1.hasMoreElements()) {
            handler = (TaskHandler) e1.nextElement();
            list.addAll(handler.getAllTasks());
        }
        return list;
    }

    public Collection getCompletedTasks() {
        // extract all the tasks from various TaskHandlers
        List list = new ArrayList();
        Enumeration e1 = this.mapping.elements();
        TaskHandler handler;
        while (e1.hasMoreElements()) {
            handler = (TaskHandler) e1.nextElement();
            list.addAll(handler.getAllTasks());
        }
        return list;
    }

    public Collection getSuspendedTasks() {
        // extract all the tasks from various TaskHandlers
        List list = new ArrayList();
        Enumeration e1 = this.mapping.elements();
        TaskHandler handler;
        while (e1.hasMoreElements()) {
            handler = (TaskHandler) e1.nextElement();
            list.addAll(handler.getAllTasks());
        }
        return list;
    }

    public Collection getResumedTasks() {
        // extract all the tasks from various TaskHandlers
        List list = new ArrayList();
        Enumeration e1 = this.mapping.elements();
        TaskHandler handler;
        while (e1.hasMoreElements()) {
            handler = (TaskHandler) e1.nextElement();
            list.addAll(handler.getAllTasks());
        }
        return list;
    }

    public Collection getCanceledTasks() {
        // extract all the tasks from various TaskHandlers
        List list = new ArrayList();
        Enumeration e1 = this.mapping.elements();
        TaskHandler handler;
        while (e1.hasMoreElements()) {
            handler = (TaskHandler) e1.nextElement();
            list.addAll(handler.getAllTasks());
        }
        return list;
    }

    private TaskHandler createTaskHandler(String provider)
            throws InvalidProviderException {
        TaskHandler taskHandler;
        try {
            taskHandler = AbstractionFactory.newFileOperationTaskHandler(provider);
        } catch (ProviderMethodException e) {
            throw new InvalidProviderException(
                    "Cannot create new task handler for provider " + provider,
                    e);
        }
        this.mapping.put(provider.toLowerCase(), taskHandler);
        return taskHandler;
    }
}