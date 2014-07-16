// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.globus.cog.abstraction.impl.common.task;

public class InvalidServiceContactException extends Exception {
    public InvalidServiceContactException(String message) {
        super(message);
    }

    public InvalidServiceContactException(String message, Throwable parent) {
        super(message, parent);
    }
}
