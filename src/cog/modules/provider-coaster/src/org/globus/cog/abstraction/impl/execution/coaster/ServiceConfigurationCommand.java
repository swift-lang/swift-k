//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 12, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import org.globus.cog.abstraction.coaster.service.job.manager.Job;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.commands.Command;

public class ServiceConfigurationCommand extends Command {
    public static final String NAME = "CONFIGSERVICE";
        
    public ServiceConfigurationCommand(Task task) {
        super(NAME);
        JobSpecification spec = (JobSpecification) task.getSpecification();
        for (String name : spec.getAttributeNames()) {
            if (!Job.SUPPORTED_ATTRIBUTES.contains(name)) {
                add(spec, name);
            }
        }
    }
    
    private void add(JobSpecification spec, String attr) {
        Object value = spec.getAttribute(attr);
        if (value != null) {
            addOutData(attr + "=" + value.toString());
        }
    }
}
