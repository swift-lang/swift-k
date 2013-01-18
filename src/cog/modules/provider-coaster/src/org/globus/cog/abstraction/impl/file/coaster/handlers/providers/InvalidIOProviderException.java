//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 1, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers.providers;

public class InvalidIOProviderException extends Exception {

    public InvalidIOProviderException() {
        super();
    }

    public InvalidIOProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidIOProviderException(String message) {
        super(message);
    }

    public InvalidIOProviderException(Throwable cause) {
        super(cause);
    }
}
