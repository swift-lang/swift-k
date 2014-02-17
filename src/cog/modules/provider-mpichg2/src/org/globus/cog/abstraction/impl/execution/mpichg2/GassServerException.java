
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.globus.cog.abstraction.impl.execution.mpichg2;

public class GassServerException extends Exception {
    public GassServerException(String message) {
        super(message);
    }

    public GassServerException(String message, Throwable parent) {
        super(message, parent);
    }
}
