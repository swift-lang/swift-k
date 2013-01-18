// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file;

/**
 * File not found exception to be thrown when the file being accessed does not
 * exist
 */
public class FileNotFoundException extends FileResourceException {
    /** Set a string message to the exception */
    public FileNotFoundException(String message) {
        super(message);
    }

    /** Set a message and throwable for the exception */
    public FileNotFoundException(String message, Throwable parent) {
        super(message, parent);
    }
}
