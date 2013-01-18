//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 23, 2007
 */
package org.globus.cog.abstraction.interfaces;

/**
 * Encapsulates various delegation settings
 */
public interface Delegation {
    /**
     * Indicates that no delegation should be performed when using this
     * credential
     */
    public static final int NO_DELEGATION = 0;
    /**
     * Indicates that full delegation should be performed when using this
     * credential. If I understand correctly, full delegation allows a
     * theoretically unlimited number of delegations to be further performed
     * from the delegated credential
     */
    public static final int FULL_DELEGATION = 1;
    /**
     * Indicates that partial/limited delegation should be performed when using
     * this credential.
     */
    public static final int PARTIAL_DELEGATION = 2;
    /**
     * Indicates that partial/limited delegation should be performed when using
     * this credential.
     */
    public static final int LIMITED_DELEGATION = PARTIAL_DELEGATION;

}
