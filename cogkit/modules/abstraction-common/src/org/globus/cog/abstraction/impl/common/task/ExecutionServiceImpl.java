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
