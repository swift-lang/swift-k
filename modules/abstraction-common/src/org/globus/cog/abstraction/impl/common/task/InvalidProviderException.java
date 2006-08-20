// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.globus.cog.abstraction.impl.common.task;

public class InvalidProviderException extends Exception {
    public InvalidProviderException(String message) {
        super(message);
    }

    public InvalidProviderException(String message, Throwable parent) {
        super(message, parent);
    }
}