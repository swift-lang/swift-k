//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 22, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.JobSpecification;

public class FakeTask extends TaskImpl {

    public FakeTask(double walltime) {
        JobSpecification js = new JobSpecificationImpl();
        js.setAttribute("maxwalltime", String.valueOf((int) walltime));
        setSpecification(js);
    }
}
