//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2005
 */
package org.globus.cog.karajan.futures;

import k.rt.Future;


public interface FutureIterator extends Future {
	boolean hasAvailable();
}
