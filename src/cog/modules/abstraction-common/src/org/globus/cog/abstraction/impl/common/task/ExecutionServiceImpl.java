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
    private String jobManager;

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
        this.jobManager = jobManager;
    }

    public void setJobManager(String jobManager) {
        this.jobManager = jobManager;
    }

    public String getJobManager() {
        return jobManager;
    }
    
    public String toString() {
        return getServiceContact() + 
               "(" + getProvider() + (jobManager == null ? "" : "/" + jobManager) + ")";
    }
}
