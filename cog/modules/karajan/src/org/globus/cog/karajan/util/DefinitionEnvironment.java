//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 8, 2006
 */
package org.globus.cog.karajan.util;

import org.globus.cog.karajan.stack.VariableStack;

public class DefinitionEnvironment {
	private VariableStack stack;
	private DefinitionEnvironment prev;
	
	public DefinitionEnvironment(VariableStack stack, DefinitionEnvironment prev) {
		this.prev = prev;
		this.stack = stack;
	}
	
	public DefinitionEnvironment(VariableStack stack) {
		this(stack, null);
	}
		
	public DefinitionEnvironment() {
		this(null);
	}
	
	public VariableStack getStack() {
		return stack;
	}

	public void setStack(VariableStack env) {
		this.stack = env;
	}

	public DefinitionEnvironment getPrev() {
		return prev;
	}

	public void setPrev(DefinitionEnvironment prev) {
		this.prev = prev;
	}
}
