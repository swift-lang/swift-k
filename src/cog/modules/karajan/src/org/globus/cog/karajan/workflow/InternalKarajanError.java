// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 27, 2005
 */
package org.globus.cog.karajan.workflow;

public class InternalKarajanError extends KarajanRuntimeException {
	private static final long serialVersionUID = 3794844129090946964L;

	public InternalKarajanError(String message) {
		this(message, null);

	}

	public InternalKarajanError(String message, Throwable cause) {
		super("Internal error. " + message, cause);
	}

	public InternalKarajanError(Throwable cause) {
		this(cause.getMessage(), cause);
	}
}