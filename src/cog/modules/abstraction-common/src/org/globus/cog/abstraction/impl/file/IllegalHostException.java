// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file;

/**
 * IllegalHostException is thrown
 */
public class IllegalHostException extends FileResourceException {
    /** Assign message for the exception */
    public IllegalHostException(String message) {
        super(message);
    }

    /** Assign message and throwable for the exception */
    public IllegalHostException(String message, Throwable parent) {
        super(message, parent);
    }
}
