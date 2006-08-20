// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public class ExecutionServiceImpl extends ServiceImpl implements
        ExecutionService {

    public ExecutionServiceImpl() {
        super(Service.JOB_SUBMISSION);
    }

    public ExecutionServiceImpl(String provider, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super(provider, Service.JOB_SUBMISSION, serviceContact, securityContext);
    }

    public ExecutionServiceImpl(String provider, ServiceContact serviceContact,
            SecurityContext securityContext, String jobManager) {
        super(provider, Service.JOB_SUBMISSION, serviceContact, securityContext);
        if (jobManager != null) {
            setAttribute("jobmanager", jobManager);
        }
    }

    public void setJobManager(String jobManager) {
        if (jobManager != null) {
            setAttribute("jobmanager", jobManager);
        }
    }

    public String getJobManager() {

        return (String) getAttribute("jobmanager");
    }

}