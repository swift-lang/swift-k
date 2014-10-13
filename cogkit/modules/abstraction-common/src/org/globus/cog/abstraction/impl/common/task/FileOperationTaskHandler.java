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

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.MultiplexingTaskHandler;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.TaskCollector;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.abstraction.interfaces.TaskHandlerCapabilities;

public class FileOperationTaskHandler extends MultiplexingTaskHandler {
    private Map<String, TaskHandler> mapping;

    public FileOperationTaskHandler() {
        this.mapping = new HashMap<String, TaskHandler>();
        setType(TaskHandler.FILE_OPERATION);
    }
    

    @Override
    public TaskHandlerCapabilities getCapabilities() {
        return TaskHandlerCapabilities.PLAIN_FILEOP;
    }

    protected TaskHandler getHandler(Task task) throws TaskSubmissionException {
        if (task.getType() != Task.FILE_OPERATION) {
            throw new TaskSubmissionException(
                    "File operation handler can only handle file operation tasks");
        }
        String provider = task.getService(0).getProvider().toLowerCase();
        TaskHandler th = null;
        synchronized (this.mapping) {
            th = this.mapping.get(provider);
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
            TaskHandler th = this.mapping.get(provider);
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
    	cancel(task, null);
    }

    public void cancel(Task task, String message) throws InvalidSecurityContextException,
            TaskSubmissionException {
        getHandler(task).cancel(task, message);
    }

    public void remove(Task task) throws ActiveTaskException {
        String provider = task.getService(0).getProvider().toLowerCase();
        TaskHandler taskHandler = this.mapping.get(provider);
        if (taskHandler != null) {
            taskHandler.remove(task);
        }
    }
    
    protected Collection<Task> getTasks(final TaskCollector collector) {
        // extract tasks from various TaskHandlers
        ArrayList<Task> list = new ArrayList<Task>();
        for (TaskHandler handler : mapping.values()) {
            list.addAll(collector.collect(handler));
        }
        return list;
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

    @Override
    public String getName() {
        return "FileOperationTaskHandler";
    }
}