//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 19, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.common;

public class ProcessException extends Exception {
	private static final long serialVersionUID = -3254385200849810535L;

	public ProcessException() {
		super();
	}

	public ProcessException(String message) {
		super(message);
	}

	public ProcessException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessException(Throwable cause) {
		super(cause);
	}
}
