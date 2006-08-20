// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class GenericTaskHandler implements TaskHandler {
    private int type;
    private TaskHandler execHandler = null;
    private TaskHandler transferHandler = null;
    private TaskHandler fileHandler = null;

    public GenericTaskHandler() {
        this.type = TaskHandler.GENERIC;
        this.execHandler = new ExecutionTaskHandler();
        this.transferHandler = new FileTransferTaskHandler();
        this.fileHandler = new FileOperationTaskHandler();
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void submit(Task task)
        throws
            IllegalSpecException,
            InvalidSecurityContextException,
            InvalidServiceContactException,
            TaskSubmissionException {
        switch (task.getType()) {
            case Task.JOB_SUBMISSION :
                this.execHandler.submit(task);
                break;
            case Task.FILE_TRANSFER :
                this.transferHandler.submit(task);
                break;
            case Task.FILE_OPERATION :
                this.fileHandler.submit(task);
                break;
            default :
                break;
        }
    }

    public void suspend(Task task)
        throws InvalidSecurityContextException, TaskSubmissionException {
        switch (task.getType()) {
            case Task.JOB_SUBMISSION :
                this.execHandler.suspend(task);
                break;
            case Task.FILE_TRANSFER :
                this.transferHandler.suspend(task);
                break;
            case Task.FILE_OPERATION :
                this.fileHandler.suspend(task);
                break;
            default :
                break;
        }
    }

    public void resume(Task task)
        throws InvalidSecurityContextException, TaskSubmissionException {
        switch (task.getType()) {
            case Task.JOB_SUBMISSION :
                this.execHandler.resume(task);
                break;
            case Task.FILE_TRANSFER :
                this.transferHandler.resume(task);
                break;
            case Task.FILE_OPERATION :
                this.fileHandler.resume(task);
                break;
            default :
                break;
        }
    }

    public void cancel(Task task)
        throws InvalidSecurityContextException, TaskSubmissionException {
        switch (task.getType()) {
            case Task.JOB_SUBMISSION :
                this.execHandler.cancel(task);
                break;
            case Task.FILE_TRANSFER :
                this.transferHandler.cancel(task);
                break;
            case Task.FILE_OPERATION :
                this.fileHandler.cancel(task);
                break;
            default :
                break;
        }
    }

    public void remove(Task task) throws ActiveTaskException {
        switch (task.getType()) {
            case Task.JOB_SUBMISSION :
                this.execHandler.remove(task);
                break;
            case Task.FILE_TRANSFER :
                this.transferHandler.remove(task);
                break;
            case Task.FILE_OPERATION :
                this.fileHandler.remove(task);
                break;
            default :
                break;
        }
    }

    public Collection getAllTasks() {
        //		extract all the tasks from various TaskHandlers
        List list = new ArrayList();
        list.addAll(this.execHandler.getAllTasks());
        list.addAll(this.transferHandler.getAllTasks());
        list.addAll(this.fileHandler.getAllTasks());
        return list;
    }

    public Collection getActiveTasks() {
        // extract all the active tasks from various TaskHandlers
        List list = new ArrayList();
        list.addAll(this.execHandler.getActiveTasks());
        list.addAll(this.transferHandler.getActiveTasks());
        list.addAll(this.fileHandler.getActiveTasks());
        return list;
    }

    public Collection getFailedTasks() {
        // extract all the failed tasks from various TaskHandlers
        List list = new ArrayList();
        list.addAll(this.execHandler.getFailedTasks());
        list.addAll(this.transferHandler.getFailedTasks());
        list.addAll(this.fileHandler.getFailedTasks());
        return list;
    }

    public Collection getCompletedTasks() {
        // extract all the completed tasks from various TaskHandlers
        List list = new ArrayList();
        list.addAll(this.execHandler.getCompletedTasks());
        list.addAll(this.transferHandler.getCompletedTasks());
        list.addAll(this.fileHandler.getCompletedTasks());
        return list;
    }

    public Collection getSuspendedTasks() {
        List list = new ArrayList();
        list.addAll(this.execHandler.getSuspendedTasks());
        list.addAll(this.transferHandler.getSuspendedTasks());
        list.addAll(this.fileHandler.getSuspendedTasks());
        return list;
    }

    public Collection getResumedTasks() {
        // extract all the resumed tasks from various TaskHandlers
        List list = new ArrayList();
        list.addAll(this.execHandler.getResumedTasks());
        list.addAll(this.transferHandler.getResumedTasks());
        list.addAll(this.fileHandler.getResumedTasks());
        return list;
    }

    public Collection getCanceledTasks() {
        // extract all the canceled tasks from various TaskHandlers
        List list = new ArrayList();
        list.addAll(this.execHandler.getCanceledTasks());
        list.addAll(this.transferHandler.getCanceledTasks());
        list.addAll(this.fileHandler.getCanceledTasks());
        return list;
    }
}