package org.griphyn.vdl.karajan;


/** Represents a SwiftScript compilation error */
public class CompilationException extends Exception {
	public CompilationException(String message) {
		super(message);
	}
	public CompilationException(String message, Exception e) {
		super(message, e);
	}
}

