//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

import java.util.Enumeration;

public class PeekableEnumeration implements Enumeration {
	private final Enumeration enumeration;
	
	private Object next;
	
	public PeekableEnumeration(Enumeration enumeration) {
		this.enumeration = enumeration;
	}
	
	public boolean hasMoreElements() {
		return (next != null) || enumeration.hasMoreElements();
	}
	
	public Object nextElement() {
		if (next != null) {
			Object n = next;
			next = null;
			return n;
		}
		return enumeration.nextElement();
	}
	
	public Object peek() {
		if (next == null) {
			next = enumeration.nextElement();
		}
		return next;
	}
}
