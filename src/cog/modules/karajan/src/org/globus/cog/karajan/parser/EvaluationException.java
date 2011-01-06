//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

public class EvaluationException extends Exception {
	private static final long serialVersionUID = 6135609764444023365L;

	public EvaluationException() {
		super();
	}

	public EvaluationException(String message) {
		this(message, null);
	}

	public EvaluationException(Throwable cause) {
		this(cause.getMessage(), cause);
	}

	public EvaluationException(String message, Throwable cause) {
		super(message, cause);
	}
}
