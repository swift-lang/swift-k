//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 20, 2007
 */
package org.globus.cog.abstraction.impl.common.execution;

public class JobException extends Exception {
    private int exitCode;
    
    public JobException(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getMessage() {
        return "Job failed with an exit code of " + exitCode;
    }
    
    public int getExitCode() {
        return exitCode;
    }
}
