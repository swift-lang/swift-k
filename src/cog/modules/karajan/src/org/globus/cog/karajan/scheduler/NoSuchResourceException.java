// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 2, 2004
 */
package org.globus.cog.karajan.scheduler;

public class NoSuchResourceException extends NoFreeResourceException {
	private static final long serialVersionUID = 4757539377378613461L;

	public NoSuchResourceException() {
		super();
	}

	public NoSuchResourceException(String message) {
		super(message);
	}

	public NoSuchResourceException(Throwable cause) {
		super(cause);
	}

	public NoSuchResourceException(String message, Throwable cause) {
		super(message, cause);
	}
}