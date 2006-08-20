// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.fileTransfer.DelegatedFileTransferHandler;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class FileTransferTaskHandler implements TaskHandler, StatusListener {
    private Vector submittedList = null;
    private Vector activeList = null;
    private Vector suspendedList = null;
    private Vector resumedList = null;
    private Vector failedList = null;
    private Vector canceledList = null;
    private Vector completedList = null;
    private Hashtable handleMap = null;
    private int type;

    public FileTransferTaskHandler() {
        this.submittedList = new Vector();
        this.activeList = new Vector();
        this.suspendedList = new Vector();
        this.resumedList = new Vector();
        this.failedList = new Vector();
        this.canceledList = new Vector();
        this.completedList = new Vector();
        this.handleMap = new Hashtable();
        this.type = TaskHandler.FILE_TRANSFER;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
    
    protected DelegatedTaskHandler getTaskHandler(Task task) {
        synchronized (this.handleMap) {
            return (DelegatedTaskHandler) this.handleMap.get(task);
        }
    }

    protected void registerTaskHandler(Task task, DelegatedTaskHandler handler) {
        synchronized (this.handleMap) {
            this.handleMap.put(task, handler);
        }
    }
    
    protected void unregisterTaskHandler(Task task) {
        synchronized (this.handleMap) {
            this.handleMap.remove(task);
        }
    }

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        if (task.getStatus().getStatusCode() != Status.UNSUBMITTED) {
            throw new TaskSubmissionException(
                    "TaskHandler can only handle unsubmitted tasks");
        }
        if (task.getType() != Task.FILE_TRANSFER) {
            throw new TaskSubmissionException(
                    "File transfer handler can only handle file transfer tasks");
        }

        DelegatedTaskHandler dth = new DelegatedFileTransferHandler();
        task.addStatusListener(this);
        registerTaskHandler(task, dth);
        dth.submit(task);
    }

    public void suspend(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        if (task.getType() != Task.FILE_TRANSFER) {
            throw new TaskSubmissionException(
                    "File transfer handler can only handle file transfer tasks");
        }
        DelegatedTaskHandler dth = getTaskHandler(task);
        if (dth != null) {
            dth.suspend();
        }
    }

    public void resume(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        if (task.getType() != Task.FILE_TRANSFER) {
            throw new TaskSubmissionException(
                    "File transfer handler can only handle file transfer tasks");
        }
        DelegatedTaskHandler dth = getTaskHandler(task);
        if (dth != null) {
            dth.resume();
        }
    }

    public void cancel(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        if (task.getType() != Task.FILE_TRANSFER) {
            throw new TaskSubmissionException(
                    "File transfer handler can only handle file transfer tasks");
        }
        DelegatedTaskHandler dth = getTaskHandler(task);
        if (dth != null) {
            dth.cancel();
        } else {
            task.setStatus(Status.CANCELED);
        }
    }
    
    public void remove(Task task) throws ActiveTaskException {
        if (!handleMap.containsKey(task)) {
            return;
        }
        int status = task.getStatus().getStatusCode();
        if ((status == Status.ACTIVE)) {
            throw new ActiveTaskException(
                    "Cannot remove an active or suspended Task");
        } else {
            // might cause problems
            // task.removeStatusListener(this);
            this.failedList.remove(task);
            this.canceledList.remove(task);
            this.completedList.remove(task);
            this.suspendedList.remove(task);
            this.submittedList.remove(task);
            this.handleMap.remove(task);

            /*
             * this is required because statusChange listeners are invoked in a
             * single thread So if one listener that wants to remove a task from
             * the handler is invoked before the listener implemented by this
             * object, the task will be be in the activeList and will not be
             * actually removed.
             * 
             * To solve this we remove tasks from active list too
             */
            this.activeList.remove(task);
        }
    }

    /** return a collection of all tasks submitted to the handler */
    public Collection getAllTasks() {
        try {
            return new ArrayList(handleMap.keySet());
        } catch (Exception e) {
            return null;
        }
    }

    /** return a collection of active tasks */
    public Collection getActiveTasks() {
        return new ArrayList(this.activeList);
    }

    /** return a collection of failed tasks */
    public Collection getFailedTasks() {
        return new ArrayList(this.failedList);
    }

    /** return a collection of completed tasks */
    public Collection getCompletedTasks() {
        return new ArrayList(this.completedList);
    }

    /** return a collection of suspended tasks */
    public Collection getSuspendedTasks() {
        return new ArrayList(this.suspendedList);
    }

    /** return a collection of resumed tasks */
    public Collection getResumedTasks() {
        return new ArrayList(this.resumedList);
    }

    /** return a collection of canceled tasks */
    public Collection getCanceledTasks() {
        return new ArrayList(this.canceledList);
    }

    public void statusChanged(StatusEvent event) {
        Task task = (Task) event.getSource();
        Status status = event.getStatus();
        int prevStatus = status.getPrevStatusCode();
        int curStatus = status.getStatusCode();
        switch (prevStatus) {
            case Status.SUBMITTED:
                this.submittedList.remove(task);
                break;
            case Status.ACTIVE:
                this.activeList.remove(task);
                break;
            case Status.SUSPENDED:
                this.suspendedList.remove(task);
                break;
            case Status.RESUMED:
                this.resumedList.remove(task);
                break;
            case Status.FAILED:
                this.failedList.remove(task);
                break;
            case Status.CANCELED:
                this.canceledList.remove(task);
                break;
            case Status.COMPLETED:
                this.completedList.remove(task);
                break;
            default:
                break;
        }
        if (task != null) {
            switch (curStatus) {
                case Status.SUBMITTED:
                    this.submittedList.add(task);
                    break;
                case Status.ACTIVE:
                    this.activeList.add(task);
                    break;
                case Status.SUSPENDED:
                    this.suspendedList.add(task);
                    break;
                case Status.RESUMED:
                    this.resumedList.add(task);
                    break;
                case Status.FAILED:
                    this.failedList.add(task);
                    unregisterTaskHandler(task);
                    break;
                case Status.CANCELED:
                    this.canceledList.add(task);
                    unregisterTaskHandler(task);
                    break;
                case Status.COMPLETED:
                    this.completedList.add(task);
                    unregisterTaskHandler(task);
                    break;
                default:
                    break;
            }
        }
    }
}