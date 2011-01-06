//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 12, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import org.globus.cog.abstraction.coaster.service.job.manager.Settings;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.commands.Command;

public class ServiceConfigurationCommand extends Command {
    public static final String NAME = "CONFIGSERVICE";
    
    public ServiceConfigurationCommand(Task task) {
        super(NAME);
        JobSpecification spec = (JobSpecification) task.getSpecification();
        for (int i = 0; i < Settings.NAMES.length; i++) {
            add(spec, Settings.NAMES[i]);
        }
    }
    
    private void add(JobSpecification spec, String attr) {
        Object value = spec.getAttribute(attr);
        if (value != null) {
            addOutData(attr + "=" + value.toString());
        }
    }
}
