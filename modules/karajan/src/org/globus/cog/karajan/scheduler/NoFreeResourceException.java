// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 2, 2004
 */
package org.globus.cog.karajan.scheduler;

public class NoFreeResourceException extends Exception {
	private static final long serialVersionUID = 4757539377378613460L;

	public NoFreeResourceException() {
		super();
	}

	public NoFreeResourceException(String message) {
		super(message);
	}

	public NoFreeResourceException(Throwable cause) {
		super(cause);
	}

	public NoFreeResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public Throwable fillInStackTrace() {
		return this;
	}
}