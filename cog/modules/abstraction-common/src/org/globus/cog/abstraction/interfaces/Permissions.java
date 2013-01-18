// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

/**
 * An object to represent the access permission of a class of users on a remote
 * File.
 */

public interface Permissions {

    /** set/unset read permissions */
    public void setRead(boolean canRead);

    /** get read permissions */
    public boolean getRead();

    /** set/unset write permissions */
    public void setWrite(boolean canWrite);

    /** get write permissions */
    public boolean getWrite();

    /** set/unset execute permissions */
    public void setExecute(boolean canExecute);

    /** get execute permissions */
    public boolean getExecute();

    /** return permissions as a string. pattern 777 */
    public String toString();
    
    public int toDigit();
}