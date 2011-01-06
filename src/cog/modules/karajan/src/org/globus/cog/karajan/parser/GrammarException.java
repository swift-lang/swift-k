//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

public class GrammarException extends RuntimeException {
	private static final long serialVersionUID = -2794611620508982802L;
	
	public GrammarException() {
		super();

	}
	public GrammarException(String message) {
		super(message);

	}
	public GrammarException(String message, Throwable cause) {
		super(message, cause);

	}
	public GrammarException(Throwable cause) {
		super(cause);

	}
}
