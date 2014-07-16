//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 11, 2008
 */
package org.globus.cog.abstraction.impl.common.task;

import org.globus.cog.abstraction.interfaces.Status;

public class StatusOrder {
    
    /**
     * Returns the predecessor of a status code or -1 if the code
     * does not have one
     */    
    public static int pred(int code) {
        switch (code) {
            case Status.CANCELED:
            	return -1;
            case Status.FAILED:
            case Status.COMPLETED:
                return Status.ACTIVE;
            case Status.SUBMITTING:
                return Status.UNSUBMITTED;
            case Status.SUBMITTED:
                return Status.SUBMITTING;
            case Status.ACTIVE:
                return Status.SUBMITTED;
            case Status.STAGE_OUT:
                return Status.ACTIVE;
            case Status.STAGE_IN:
                return Status.SUBMITTED;
            case Status.RESUMED:
                return Status.SUSPENDED;
            default:
                return -1;
        }
    }
    
    /**
     * Returns true code1 is greater than code2 in the status
     * order. The two need not be comparable (in which case false 
     * will be returned).
     */
    public static boolean greaterThan(int code1, int code2) {
        int prev = pred(code1);
        if (prev == -1) {
            return false;
        }
        else {
            return prev == code2 || greaterThan(prev, code2);
        }
    }
}
