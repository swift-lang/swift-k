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