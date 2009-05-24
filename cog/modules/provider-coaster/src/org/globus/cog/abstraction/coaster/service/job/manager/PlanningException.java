//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 11, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

public class PlanningException extends Exception {

    public PlanningException() {
        super();
    }

    public PlanningException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlanningException(String message) {
        super(message);
    }

    public PlanningException(Throwable cause) {
        super(cause);     
    }
}
