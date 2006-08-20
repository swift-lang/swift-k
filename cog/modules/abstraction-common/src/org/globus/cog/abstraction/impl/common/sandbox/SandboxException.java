// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 23, 2004
 */
package org.globus.cog.abstraction.impl.common.sandbox;

public class SandboxException extends RuntimeException {

	public SandboxException() {
		super();
	}

	public SandboxException(String message) {
		super(message);
	}

	public SandboxException(String message, Throwable cause) {
		super(message, cause);
	}

	public SandboxException(Throwable cause) {
		super(cause);
	}
}