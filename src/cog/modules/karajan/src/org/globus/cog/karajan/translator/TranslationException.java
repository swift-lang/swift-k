//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

//How can one distinguish between removing the message above
//and creating the rest of the file from scratch? It's not 
//such an uncommon piece of code.

/*
 * Created on Apr 14, 2005
 */
package org.globus.cog.karajan.translator;

public class TranslationException extends Exception {
	private static final long serialVersionUID = 213204842589266034L;

	public TranslationException() {
		super();
	}
	
	public TranslationException(String message) {
		super(message);
	}

	public TranslationException(Throwable cause) {
		super(cause);
	}

	public TranslationException(String message, Throwable cause) {
		super(message, cause);
	}

}
