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
