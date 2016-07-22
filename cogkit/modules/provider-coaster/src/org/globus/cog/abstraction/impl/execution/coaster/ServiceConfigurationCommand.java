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
 * Created on Feb 12, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.commands.Command;

public class ServiceConfigurationCommand extends Command {
    public static final String NAME = "CONFIGSERVICE";
        
    public ServiceConfigurationCommand(Task task) {
        super(NAME);
        
        String jm = null;
        Service s = task.getService(0);
        if (s instanceof ExecutionService) {
        	jm = ((ExecutionService) s).getJobManager();
        }
        
        String os = (String) s.getAttribute("OS");
        addOutData("OS=" + os);
        if (jm != null) {
            int colon = jm.indexOf(':');
            // remove provider used to bootstrap coasters
            jm = jm.substring(colon + 1);
            colon = jm.indexOf(':');
            if (colon == -1) {
                addOutData("provider=" + jm);
            }
            else {
                addOutData("jobManager=" + jm.substring(colon + 1));
                addOutData("provider=" + jm.substring(0, colon));
            }
            addOutData("serviceContact=" + s.getServiceContact());
        }
        JobSpecification spec = (JobSpecification) task.getSpecification();
        for (String name : s.getAttributeNames()) {
            add(s, name);
        }
    }
    
    private void add(Service s, String attr) {
        if (attr.startsWith("#")) {
            return;
        }
        Object value = s.getAttribute(attr);
        if (value != null) {
            addOutData(attr + "=" + value.toString());
        }
    }
}
