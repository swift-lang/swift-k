/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.TaskHandlerSkeleton;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.abstraction.interfaces.TaskHandlerCapabilities;

public class ExecutionTaskHandler extends TaskHandlerSkeleton {
    
    private Map<String, TaskHandler> mapping;
    Logger logger = Logger.getLogger(ExecutionTaskHandler.class);
    
    public ExecutionTaskHandler() {
        mapping = new HashMap<String, TaskHandler>();
        setType(TaskHandler.EXECUTION);
    }

    @Override
    public TaskHandlerCapabilities getCapabilities() {
        // can't know until a task is submitted, so lowest common denominator
        return TaskHandlerCapabilities.EXEC_NO_STAGING;
    }

    public void submit(Task task)
        throws
            IllegalSpecException,
            InvalidSecurityContextException,
            InvalidServiceContactException,
            TaskSubmissionException {
        if (task.getType() != Task.JOB_SUBMISSION) {
            throw new TaskSubmissionException
                ("Execution handler can only handle job submission tasks");
        }
        String provider = task.getService(0).getProvider().toLowerCase();
        if (logger.isInfoEnabled()) {
            logger.info("provider=" + provider);
        }
        TaskHandler taskHandler = mapping.get(provider);

        if (taskHandler == null) {
            try {
                taskHandler = createTaskHandler(provider);
            } catch (InvalidProviderException ipe) {
                throw new TaskSubmissionException("Cannot submit task", ipe);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("taskHandler="+taskHandler);
        }
        taskHandler.submit(task);
    }

    public void suspend(Task task)
        throws InvalidSecurityContextException, TaskSubmissionException {
        if (task.getType() != Task.JOB_SUBMISSION) {
            throw new TaskSubmissionException("Execution handler can only handle job submission tasks");
        }
        String provider = task.getService(Service.DEFAULT_SERVICE).getProvider().toLowerCase();
        TaskHandler taskHandler = this.mapping.get(provider);
        if (taskHandler != null) {
            taskHandler.suspend(task);
        } else {
            throw new TaskSubmissionException(
                "Provider " + provider + " unknown");
        }
    }

    public void resume(Task task)
        throws InvalidSecurityContextException, TaskSubmissionException {
        if (task.getType() != Task.JOB_SUBMISSION) {
            throw new TaskSubmissionException("Execution handler can only handle job submission tasks");
        }
        String provider = task.getService(Service.DEFAULT_SERVICE).getProvider().toLowerCase();
        TaskHandler taskHandler = this.mapping.get(provider);
        if (taskHandler != null) {
            taskHandler.resume(task);
        } else {
            throw new TaskSubmissionException(
                "Provider " + provider + " unknown");
        }
    }
    
    public void cancel(Task task) throws InvalidSecurityContextException, TaskSubmissionException {
        cancel(task, null);
    }

    public void cancel(Task task, String message)
        throws InvalidSecurityContextException, TaskSubmissionException {
        if (task.getType() != Task.JOB_SUBMISSION) {
            throw new TaskSubmissionException("Execution handler can only handle job submission tasks");
        }
        String provider = task.getService(Service.DEFAULT_SERVICE).getProvider().toLowerCase();
        TaskHandler taskHandler = this.mapping.get(provider);
        if (taskHandler != null) {
            taskHandler.cancel(task, message);
        } else {
            task.setStatus(new StatusImpl(Status.CANCELED, message, null));
        }
    }

    public void remove(Task task) throws ActiveTaskException {
        String provider = task.getService(Service.DEFAULT_SERVICE).getProvider().toLowerCase();
        TaskHandler taskHandler = this.mapping.get(provider);
        if (taskHandler != null) {
            taskHandler.remove(task);
        }
    }
    
    public Collection<Task> getAllTasks() {
        ArrayList<Task> l = new ArrayList<Task>();
        synchronized(mapping) {
            for (TaskHandler th : mapping.values()) {
                l.addAll(th.getAllTasks());
            }
        }
        return l;
    }
    
    protected Collection<Task> getTasksWithStatus(int code) {
        ArrayList<Task> l = new ArrayList<Task>();
        synchronized(mapping) {
            for (TaskHandler th : mapping.values()) {
                for (Task t : th.getAllTasks()) {
                    if (t.getStatus().getStatusCode() == code) {
                        l.add(t);
                    }
                }
            }
        }
        return l;
    }

    private TaskHandler createTaskHandler(String provider)
        throws InvalidProviderException {
        TaskHandler taskHandler;
        try {
            taskHandler = AbstractionFactory.newExecutionTaskHandler(provider);
        } catch (ProviderMethodException e) {
            throw new InvalidProviderException(
                "Cannot create new task handler for provider " + provider,
                e);
        }
        this.mapping.put(provider, taskHandler);
        return taskHandler;
    }
    
    /** return a collection of active tasks */
    public Collection<Task> getActiveTasks() {
        return getTasksWithStatus(Status.ACTIVE);
    }

    /** return a collection of failed tasks */
    public Collection<Task> getFailedTasks() {
        return getTasksWithStatus(Status.FAILED);
    }

    /** return a collection of completed tasks */
    public Collection<Task> getCompletedTasks() {
        return getTasksWithStatus(Status.COMPLETED);
    }

    /** return a collection of suspended tasks */
    public Collection<Task> getSuspendedTasks() {
        return getTasksWithStatus(Status.SUSPENDED);
    }

    /** return a collection of resumed tasks */
    public Collection<Task> getResumedTasks() {
        return getTasksWithStatus(Status.RESUMED);
    }

    /** return a collection of canceled tasks */
    public Collection<Task> getCanceledTasks() {
        return getTasksWithStatus(Status.CANCELED);
    }
    
    public String getName() {
        return "ExecutionTaskHandler"; 
    }
}
