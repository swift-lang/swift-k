// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 27, 2005
 */
package org.globus.cog.karajan.workflow;

public class KarajanRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -6253790911180606347L;

	public KarajanRuntimeException() {
		super();
	}

	public KarajanRuntimeException(String message) {
		super(message);
	}

	public KarajanRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public KarajanRuntimeException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
}