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

public abstract class MultiplexingTaskHandler extends TaskHandlerSkeleton {

    protected abstract Collection getTasks(final TaskCollector collector);
    
    public Collection getAllTasks() {
        return getTasks(TaskCollector.COLLECTOR_ALL);
    }

    public Collection getActiveTasks() {
        return getTasks(TaskCollector.COLLECTOR_ACTIVE);
    }

    public Collection getFailedTasks() {
        return getTasks(TaskCollector.COLLECTOR_FAILED);
    }

    public Collection getCompletedTasks() {
        return getTasks(TaskCollector.COLLECTOR_COMPLETED);
    }

    public Collection getSuspendedTasks() {
        return getTasks(TaskCollector.COLLECTOR_SUSPENDED);
    }

    public Collection getResumedTasks() {
        return getTasks(TaskCollector.COLLECTOR_RESUMED);
    }

    public Collection getCanceledTasks() {
        return getTasks(TaskCollector.COLLECTOR_CANCELED);
    }

}
