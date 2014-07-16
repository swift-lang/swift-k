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

    /** get read permissions */
    public boolean getRead();

    /** get write permissions */
    public boolean getWrite();

    /** get execute permissions */
    public boolean getExecute();

    /** return permissions as a string. pattern 777 */
    public String toString();
    
    public int toDigit();
}