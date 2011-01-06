// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 18, 2004
 */
package org.globus.cog.karajan.util;

import java.util.Iterator;

public interface KarajanIterator extends Iterator {
	int current();
	
	int count();
	
	Object peek();
}