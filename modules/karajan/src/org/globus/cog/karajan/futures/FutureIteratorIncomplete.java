//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 16, 2005
 */
package org.globus.cog.karajan.futures;

import k.rt.Future;

public class FutureIteratorIncomplete extends FutureNotYetAvailable {
	private static final long serialVersionUID = 32776920138779658L;
	
	private final FutureIterator iterator;
	
	public FutureIteratorIncomplete(Future f, FutureIterator i) {
		super(f);
		this.iterator = i;
	}
	
	public FutureIterator getFutureIterator() {
		return iterator;
	}
}
