//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 27, 2007
 */
package org.globus.cog.abstraction.impl.file;

/**
 * Signifies an exception that puts a file resource in an irrecoverable
 * state. If a resource throws this exception during a request, future
 * requests on this resource will will fail or cause unpredictable
 * behavior.
 */
public class IrrecoverableResourceException extends FileResourceException {

	public IrrecoverableResourceException() {
		super();
	}

	public IrrecoverableResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public IrrecoverableResourceException(String message) {
		super(message);
	}

	public IrrecoverableResourceException(Throwable cause) {
		super(cause);
	}
}
