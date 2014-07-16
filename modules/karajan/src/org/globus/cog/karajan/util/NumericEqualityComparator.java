//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.util;

public class NumericEqualityComparator extends AbstractEqualityComparator {

	protected boolean compareOne(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		if (o2 == null) {
			return false;
		}
		
		return toNumber(o1) == toNumber(o2); 
	}
	
	public double toNumber(Object o) {
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		if (o instanceof String) {
			try {
				return Double.parseDouble((String) o);
			}
			catch (Exception e) {
				return Double.NaN;
			}
		}
		return Double.NaN;
	}

}
