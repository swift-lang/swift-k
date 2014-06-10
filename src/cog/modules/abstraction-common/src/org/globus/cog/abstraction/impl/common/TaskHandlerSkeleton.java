//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 4, 2009
 */
package org.globus.cog.abstraction.impl.common;

import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public abstract class TaskHandlerSkeleton implements TaskHandler {

    private int type;
    
    public void cancel(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        cancel(task, null);
    }

    public void setType(int type) {
    	this.type = type;
    }

    public int getType() {
    	return type;
    }
    
    public String toString() {
        return "TaskHandler(type = " + getType() + ", provider = " + getName() + ")";
    }
    
    public void setName(String name) {
        // ignored
    }
}
