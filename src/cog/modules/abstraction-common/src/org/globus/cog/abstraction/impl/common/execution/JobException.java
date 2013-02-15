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

    private static final long serialVersionUID = 1L;
    
    private int exitCode;
    private String message;
    
    public JobException(int exitCode) {
        this.exitCode = exitCode;
        this.message = "Job failed with an exit code of " + exitCode;
    }
    
    public JobException(String message, int exitCode) {
        this.exitCode = exitCode;
        this.message = message + " (exit code: " + exitCode + ")";
    }

    public String getMessage() {
        return message;
    }
    
    public int getExitCode() {
        return exitCode;
    }
}
