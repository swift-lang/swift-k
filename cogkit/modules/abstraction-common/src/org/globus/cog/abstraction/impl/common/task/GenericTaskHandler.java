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

import org.globus.cog.abstraction.impl.common.MultiplexingTaskHandler;
import org.globus.cog.abstraction.impl.common.TaskCollector;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.abstraction.interfaces.TaskHandlerCapabilities;

public class GenericTaskHandler extends MultiplexingTaskHandler {
    private TaskHandler execHandler;
    private TaskHandler transferHandler;
    private TaskHandler fileHandler;

    public GenericTaskHandler() {
        setType(TaskHandler.GENERIC);
        this.execHandler = new ExecutionTaskHandler();
        this.transferHandler = new FileTransferTaskHandler();
        this.fileHandler = new FileOperationTaskHandler();
    }

    @Override
    public TaskHandlerCapabilities getCapabilities() {
        return TaskHandlerCapabilities.NONE;
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
    
    public void cancel(Task task, String message)
        throws InvalidSecurityContextException, TaskSubmissionException {
        switch (task.getType()) {
            case Task.JOB_SUBMISSION :
                this.execHandler.cancel(task, message);
                break;
            case Task.FILE_TRANSFER :
                this.transferHandler.cancel(task, message);
                break;
            case Task.FILE_OPERATION :
                this.fileHandler.cancel(task, message);
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
    
    protected Collection<Task> getTasks(final TaskCollector collector) {
        // extract tasks from various TaskHandlers
        ArrayList<Task> list = new ArrayList<Task>();
        list.addAll(collector.collect(execHandler));
        list.addAll(collector.collect(transferHandler));
        list.addAll(collector.collect(fileHandler));
        return list;
    }
    
    public String getName() {
        return "GenericTaskHandler";
    }
}