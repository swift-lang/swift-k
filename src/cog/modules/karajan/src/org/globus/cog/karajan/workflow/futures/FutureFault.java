//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.karajan.workflow.futures;

public class FutureFault extends RuntimeException {
	private static final long serialVersionUID = -6674574531012966783L;
	
	private Future f;
	public static long count;

	public FutureFault(Future f) {
		this(f, null);
		count++;
	}
	
	public FutureFault(Future f, String op) {
		super(op);
		this.f = f;
		count++;
	}
	
	public Future getFuture() {
		return f;
	}
}
