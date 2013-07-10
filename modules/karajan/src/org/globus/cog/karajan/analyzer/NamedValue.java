//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 7, 2012
 */
package org.globus.cog.karajan.analyzer;


public class NamedValue {
	public final String ns;
	public final String name;
	public final Object value;
	
	public NamedValue(String ns, String name, Object def) {
		this.ns = ns;
		this.name = name;
		this.value = def;
	}
	
	public String toString() {
		return ns + ":" + name + " -> " + value;
	}
}
