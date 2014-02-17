//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 5, 2012
 */
package org.globus.cog.karajan.analyzer;

public class Parameter {
	public enum Type {
		CHANNEL, POSITIONAL, OPTIONAL, RETURN_CHANNEL
	}
	
	public final String name;
	public final Type type;
	
	public Parameter(String name, Type type) {
		this.name = name;
		this.type = type;
	}
}
