// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2005
 */
package org.globus.cog.karajan.workflow.futures;


public class FutureNotYetAvailable extends FutureFault {
	private static final long serialVersionUID = -6211457624313265733L;

	public FutureNotYetAvailable(Future f) {
		super(f);
	}

	public FutureNotYetAvailable(Future f, String op) {
		super(f, null);
	}
}