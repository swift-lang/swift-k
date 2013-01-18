//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

public class UndefinedVariableException extends EvaluationException {
	private static final long serialVersionUID = 725532357079109132L;

	public UndefinedVariableException() {
		super();

	}

	public UndefinedVariableException(String message) {
		super(message);

	}

	public UndefinedVariableException(Throwable cause) {
		super(cause);

	}

	public UndefinedVariableException(String message, Throwable cause) {
		super(message, cause);

	}

}
