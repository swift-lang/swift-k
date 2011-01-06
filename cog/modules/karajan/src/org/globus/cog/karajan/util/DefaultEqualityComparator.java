//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.util;

public class DefaultEqualityComparator extends AbstractEqualityComparator {

	protected boolean compareOne(Object o1, Object o2) {
		if (o1 != null) {
			return o1.equals(o2);
		}
		return o2 == null;
	}

}
