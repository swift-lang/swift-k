//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.coaster;

public class NoSuchHandlerException extends Exception {
	private static final long serialVersionUID = -3733405556778308146L;

	public NoSuchHandlerException(String request) {
		this(request, null);
	}

	public NoSuchHandlerException(String request, Throwable cause) {
		super("Unknown command: " + request, cause);
	}
}
