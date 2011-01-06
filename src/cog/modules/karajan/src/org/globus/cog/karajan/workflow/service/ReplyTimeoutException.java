//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 2, 2005
 */
package org.globus.cog.karajan.workflow.service;

public class ReplyTimeoutException extends ProtocolException {
	private static final long serialVersionUID = -6781619140427115780L;

	public ReplyTimeoutException() {
		super();
	}
	
	public ReplyTimeoutException(String message) {
		super(message);
	}

	public ReplyTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReplyTimeoutException(Throwable cause) {
		super(cause);
	}
}
