//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class AssociatedTask {
    public final Task task;
    public final WallTime maxWallTime; 
    
    public AssociatedTask(Task task) {
        this.task = task;
        this.maxWallTime = getMaxWallTime(task);
    }
    
    public static WallTime getMaxWallTime(Task t) {
        Object wt = ((JobSpecification) t.getSpecification()).getAttribute("maxwalltime");
        if (wt == null) {
            return new WallTime("10");
        }
        else {
            return new WallTime(wt.toString());
        }
    }
    
    public String toString() {
        return "AT/" + task.getIdentity();
    }
    
    public WallTime getMaxWallTime() {
        return maxWallTime;
    }
}
