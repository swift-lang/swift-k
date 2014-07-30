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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 4, 2009
 */
package org.globus.cog.abstraction.impl.common;

import java.util.Collection;

import org.globus.cog.abstraction.interfaces.Task;

public abstract class MultiplexingTaskHandler extends TaskHandlerSkeleton {

    protected abstract Collection<Task> getTasks(final TaskCollector collector);
    
    public Collection<Task> getAllTasks() {
        return getTasks(TaskCollector.COLLECTOR_ALL);
    }

    public Collection<Task> getActiveTasks() {
        return getTasks(TaskCollector.COLLECTOR_ACTIVE);
    }

    public Collection<Task> getFailedTasks() {
        return getTasks(TaskCollector.COLLECTOR_FAILED);
    }

    public Collection<Task> getCompletedTasks() {
        return getTasks(TaskCollector.COLLECTOR_COMPLETED);
    }

    public Collection<Task> getSuspendedTasks() {
        return getTasks(TaskCollector.COLLECTOR_SUSPENDED);
    }

    public Collection<Task> getResumedTasks() {
        return getTasks(TaskCollector.COLLECTOR_RESUMED);
    }

    public Collection<Task> getCanceledTasks() {
        return getTasks(TaskCollector.COLLECTOR_CANCELED);
    }

}
