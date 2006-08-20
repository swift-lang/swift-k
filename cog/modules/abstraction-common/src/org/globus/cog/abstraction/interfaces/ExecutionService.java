// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

public interface ExecutionService extends Service {
    public static final String FORK_JOBMANAGER = "FORK";
    public static final String PBS_JOBMANAGER = "PBS";

    public void setJobManager(String jobManager);

    public String getJobManager();
}