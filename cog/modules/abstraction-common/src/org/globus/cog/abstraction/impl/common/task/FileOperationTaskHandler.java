// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class FileOperationTaskHandler implements TaskHandler {
    private Map mapping;
    private int type;

    public FileOperationTaskHandler() {
        this.mapping = new HashMap();
        this.type = TaskHandler.FILE_OPERATION;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    protected TaskHandler getHandler(Task task) throws TaskSubmissionException {
        if (task.getType() != Task.FILE_OPERATION) {
            throw new TaskSubmissionException(
                    "File operation handler can only handle file operation tasks");
        }
        String provider = task.getService(0).getProvider().toLowerCase();
        TaskHandler th = null;
        synchronized (this.mapping) {
            th = (TaskHandler) this.mapping.get(provider);
        }
        if (th == null) {
            throw new TaskSubmissionException("Provider " + provider
                    + " unknown");
        }
        else {
            return th;
        }
    }

    protected TaskHandler getOrCreateHandler(Task task)
            throws TaskSubmissionException, InvalidProviderException {
        if (task.getType() != Task.FILE_OPERATION) {
            throw new TaskSubmissionException(
                    "File operation handler can only handle file operation tasks");
        }
        String provider = task.getService(0).getProvider().toLowerCase();
        synchronized (this.mapping) {
            TaskHandler th = (TaskHandler) this.mapping.get(provider);
            if (th == null) {
                th = createTaskHandler(task);
            }
            return th;
        }
    }

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        TaskHandler taskHandler;
        task.setStatus(Status.SUBMITTING);
        try {
            taskHandler = getOrCreateHandler(task);
        }
        catch (InvalidProviderException ipe) {
            throw new TaskSubmissionException("Cannot submit task", ipe);
        }
        taskHandler.submit(task);
    }

    public void suspend(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        getHandler(task).suspend(task);
    }

    public void resume(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        getHandler(task).suspend(task);
    }

    public void cancel(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        getHandler(task).cancel(task);
    }

    public void remove(Task task) throws ActiveTaskException {
        String provider = task.getService(0).getProvider().toLowerCase();
        TaskHandler taskHandler = (TaskHandler) this.mapping.get(provider);
        if (taskHandler != null) {
            taskHandler.remove(task);
        }
    }

    private static interface Collector {
        Collection collect(TaskHandler th);
    }

    public static final Collector COLLECTOR_ALL = new Collector() {
        public Collection collect(TaskHandler th) {
            return th.getAllTasks();
        }
    };

    public static final Collector COLLECTOR_ACTIVE = new Collector() {
        public Collection collect(TaskHandler th) {
            return th.getActiveTasks();
        }
    };

    public static final Collector COLLECTOR_SUSPENDED = new Collector() {
        public Collection collect(TaskHandler th) {
            return th.getSuspendedTasks();
        }
    };

    public static final Collector COLLECTOR_RESUMED = new Collector() {
        public Collection collect(TaskHandler th) {
            return th.getResumedTasks();
        }
    };

    public static final Collector COLLECTOR_COMPLETED = new Collector() {
        public Collection collect(TaskHandler th) {
            return th.getCompletedTasks();
        }
    };

    public static final Collector COLLECTOR_FAILED = new Collector() {
        public Collection collect(TaskHandler th) {
            return th.getFailedTasks();
        }
    };

    public static final Collector COLLECTOR_CANCELED = new Collector() {
        public Collection collect(TaskHandler th) {
            return th.getCanceledTasks();
        }
    };

    public Collection getAllTasks() {
        return getTasks(COLLECTOR_ALL);
    }

    private Collection getTasks(final Collector collector) {
        // extract tasks from various TaskHandlers
        ArrayList list = new ArrayList();
        Iterator i = this.mapping.values().iterator();
        while (i.hasNext()) {
            TaskHandler handler = (TaskHandler) i.next();
            list.addAll(collector.collect(handler));
        }
        return list;
    }

    public Collection getActiveTasks() {
        // extract all the active tasks from various TaskHandlers
        return getTasks(COLLECTOR_ACTIVE);
    }

    public Collection getFailedTasks() {
        // extract all the failed tasks from various TaskHandlers
        return getTasks(COLLECTOR_FAILED);
    }

    public Collection getCompletedTasks() {
        // extract all the tasks from various TaskHandlers
        return getTasks(COLLECTOR_COMPLETED);
    }

    public Collection getSuspendedTasks() {
        // extract all the tasks from various TaskHandlers
        return getTasks(COLLECTOR_SUSPENDED);
    }

    public Collection getResumedTasks() {
        // extract all the tasks from various TaskHandlers
        return getTasks(COLLECTOR_RESUMED);
    }

    public Collection getCanceledTasks() {
        // extract all the tasks from various TaskHandlers
        return getTasks(COLLECTOR_CANCELED);
    }

    private TaskHandler createTaskHandler(Task task)
            throws InvalidProviderException {
        String provider = task.getService(0).getProvider().toLowerCase();
        TaskHandler taskHandler;
        try {
            taskHandler = AbstractionFactory
                    .newFileOperationTaskHandler(provider);
        }
        catch (ProviderMethodException e) {
            throw new InvalidProviderException(
                    "Cannot create new task handler for provider " + provider,
                    e);
        }
        this.mapping.put(provider.toLowerCase(), taskHandler);
        return taskHandler;
    }
}