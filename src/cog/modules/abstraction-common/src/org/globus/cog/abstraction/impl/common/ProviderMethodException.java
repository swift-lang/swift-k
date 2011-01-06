// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;


public class ProviderMethodException extends Exception {

	public ProviderMethodException() {
		super();
	}

	public ProviderMethodException(String message) {
		super(message);
	}

	public ProviderMethodException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProviderMethodException(Throwable cause) {
		super(cause);
	}
}