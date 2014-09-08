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
        if (t == null) {
            return null;
        }
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
