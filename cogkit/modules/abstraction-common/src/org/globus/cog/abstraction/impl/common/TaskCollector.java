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
import org.globus.cog.abstraction.interfaces.TaskHandler;

public interface TaskCollector {
    Collection<Task> collect(TaskHandler th);

    public static final TaskCollector COLLECTOR_ALL = new TaskCollector() {
        public Collection<Task> collect(TaskHandler th) {
            return th.getAllTasks();
        }
    };

    public static final TaskCollector COLLECTOR_ACTIVE = new TaskCollector() {
        public Collection<Task> collect(TaskHandler th) {
            return th.getActiveTasks();
        }
    };

    public static final TaskCollector COLLECTOR_SUSPENDED = new TaskCollector() {
        public Collection<Task> collect(TaskHandler th) {
            return th.getSuspendedTasks();
        }
    };

    public static final TaskCollector COLLECTOR_RESUMED = new TaskCollector() {
        public Collection<Task> collect(TaskHandler th) {
            return th.getResumedTasks();
        }
    };

    public static final TaskCollector COLLECTOR_COMPLETED = new TaskCollector() {
        public Collection<Task> collect(TaskHandler th) {
            return th.getCompletedTasks();
        }
    };

    public static final TaskCollector COLLECTOR_FAILED = new TaskCollector() {
        public Collection<Task> collect(TaskHandler th) {
            return th.getFailedTasks();
        }
    };

    public static final TaskCollector COLLECTOR_CANCELED = new TaskCollector() {
        public Collection<Task> collect(TaskHandler th) {
            return th.getCanceledTasks();
        }
    };
}