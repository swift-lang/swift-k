
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 8, 2003
 */
package org.globus.cog.karajan.util;

public class VirtualContact extends Contact{
		
	public boolean isVirtual() {
		return true;
	}
	
	public String toString() {
		return String.valueOf(getId());
	}
}
