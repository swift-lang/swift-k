//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 6, 2005
 */
package org.globus.cog.karajan.util;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;

public class Identifier {
	private final String name;
	
	public Identifier(String name) {
		this.name = name.toLowerCase();
	}
	
	public Object getValue(VariableStack stack) throws VariableNotFoundException {
		return stack.getVar(name);
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
}
