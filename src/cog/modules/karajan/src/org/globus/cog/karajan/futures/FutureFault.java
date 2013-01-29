//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.karajan.futures;

import k.rt.ConditionalYield;
import k.rt.FutureValue;

public class FutureFault extends ConditionalYield {
	private static final long serialVersionUID = -6674574531012966783L;
	
	private FutureValue fv;
	public static long count;

	public FutureFault(FutureValue f) {
		this(f, null);
		count++;
	}
	
	public FutureFault(FutureValue f, String op) {
		super(f);
		this.fv = f;
		count++;
	}
	
	public FutureValue getFutureValue() {
		return fv;
	}	
}
