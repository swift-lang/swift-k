//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 6, 2005
 */
package org.globus.cog.coaster.channels;

public class ChannelException extends Exception {
	private static final long serialVersionUID = -2663954171954524659L;

	public ChannelException() {
		super();
	}

	public ChannelException(String message) {
		super(message);
	}

	public ChannelException(Throwable cause) {
		super(cause);
	}

	public ChannelException(String message, Throwable cause) {
		super(message, cause);
	}
}
