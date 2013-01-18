// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import java.io.Serializable;
import java.util.Date;

/**
 * An execution status associated with an <code>ExecutableObject</code>.
 */
public interface Status extends Serializable {

    /**
     * The <code>ExecutableObject</code> is not submitted to the remote
     * service.
     */
    public static final int UNSUBMITTED = 0;
    
    /**
     * This is set right before submission.
     */
    public static final int SUBMITTING = 8;

    /**
     * The <code>ExecutableObject</code> is submitted to the remote service
     * but not yet remotely executed.
     */
    public static final int SUBMITTED = 1;

    /**
     * The <code>ExecutableObject</code> is being remotely executed.
     */
    public static final int ACTIVE = 2;

    /**
     * Remote execution of the <code>ExecutableObject</code> is suspended.
     */
    public static final int SUSPENDED = 3;

    /**
     * The remote execution of the suspended <code>ExecutableObject</code> is
     * being resumed.
     */
    public static final int RESUMED = 4;

    /**
     * Remote execution of the <code>ExecutableObject</code> has failed.
     */
    public static final int FAILED = 5;

    /**
     * The execution of the <code>ExecutableObject</code> has been canceled.
     */
    public static final int CANCELED = 6;

    /**
     * Remote execution of the <code>ExecutableObject</code> completed
     * successfully.
     */
    public static final int COMPLETED = 7;
    
    public static final int STAGE_IN = 16;
    
    public static final int STAGE_OUT = 17;

    /**
     * Unknown status.
     */
    public static final int UNKNOWN = 9999;

    /**
     * Sets the code of this <code>Status</code>.
     * 
     * @param status
     *            an integer representing the code of this <code>Status</code>
     */
    public void setStatusCode(int status);

    /**
     * Returns the code of this <code>Status</code>
     */
    public int getStatusCode();

    /**
     * Returns the value of this <code>Status</code> as a String-based
     * description
     * 
     * @return
     */
    public String getStatusString();

    public void setPrevStatusCode(int status);

    public int getPrevStatusCode();

    public String getPrevStatusString();

    /**
     * Sets the exception associated with the failure of the
     * <code>ExecutableObject</code>. Valid only if the
     * <code>ExecutableObject</code> has failed.
     */
    public void setException(Exception exception);

    /**
     * Returns the exception associated with a failed <code>Status</code>
     */
    public Exception getException();

    /**
     * Sets the message associated with the failure of the
     * <code>ExecutableObject</code>. Valid only if the
     * <code>ExecutableObject</code> has failed.
     */
    public void setMessage(String message);

    /**
     * Returns the message associated with a failed <code>Status</code>
     */
    public String getMessage();

    /**
     * Sets the time when the current status changed
     */
    public void setTime(Date time);

    /**
     * Returns the time when the current status changed.
     */
    public Date getTime();
    
    /**
     * Returns <code>true</code> if this status is a terminal
     * status in the task life cycle. For example, <code>COMPLETED</code>,
     * <code>FAILED</code>, and <code>CANCELED</code> are terminal states.
     */
    public boolean isTerminal();
}