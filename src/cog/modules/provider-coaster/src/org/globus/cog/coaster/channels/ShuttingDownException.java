//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 7, 2005
 */
package org.globus.cog.coaster.channels;

public class ShuttingDownException extends ChannelException {
	private static final long serialVersionUID = -2932192906651305794L;

	public ShuttingDownException() {
		super();

	}

	public ShuttingDownException(String message) {
		super(message);

	}

	public ShuttingDownException(Throwable cause) {
		super(cause);

	}

	public ShuttingDownException(String message, Throwable cause) {
		super(message, cause);

	}

}
