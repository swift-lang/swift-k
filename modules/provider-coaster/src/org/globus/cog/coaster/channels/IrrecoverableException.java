//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 24, 2009
 */
package org.globus.cog.coaster.channels;

public class IrrecoverableException extends Exception {

	public IrrecoverableException() {
		super();
	}

	public IrrecoverableException(String message, Throwable cause) {
		super(message, cause);
	}

	public IrrecoverableException(String message) {
		super(message);	
	}

	public IrrecoverableException(Throwable cause) {
		super(cause);	
	}	
}
