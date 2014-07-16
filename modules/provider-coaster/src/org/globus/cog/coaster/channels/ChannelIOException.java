//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.coaster.channels;

public class ChannelIOException extends RuntimeException {
	private static final long serialVersionUID = 8369798703703264224L;

	public ChannelIOException() {
		super();
	}

	public ChannelIOException(String message) {
		super(message);
	}

	public ChannelIOException(Throwable cause) {
		super(cause);
	}

	public ChannelIOException(String message, Throwable cause) {
		super(message, cause);
	}
}
