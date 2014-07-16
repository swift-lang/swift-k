// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;


public class InvalidClassException extends Exception {

	public InvalidClassException() {
		super();
	}

	public InvalidClassException(String message) {
		super(message);
	}

	public InvalidClassException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidClassException(Throwable cause) {
		super(cause);
	}
}