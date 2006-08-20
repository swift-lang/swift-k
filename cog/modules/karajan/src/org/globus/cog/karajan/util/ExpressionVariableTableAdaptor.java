//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.util;

import org.globus.cog.karajan.parser.EvaluationContext;
import org.globus.cog.karajan.parser.UndefinedVariableException;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.futures.Future;


public class ExpressionVariableTableAdaptor implements EvaluationContext {
	private final VariableStack stack;

	public ExpressionVariableTableAdaptor(VariableStack stack) {
		this.stack = stack;
	}

	public Object get(String name) throws UndefinedVariableException {
		try {
			Object value = stack.getVar(name);
			if (value instanceof Future) {
				return ((Future) value).getValue();
			}
			else {
				return value;
			}
		}
		catch (VariableNotFoundException e) {
			throw new UndefinedVariableException(e.getMessage());
		}
	}

	public boolean hasVariable(String name) {
		return stack.isDefined(name);
	}
	
	public String toString() {
		return stack.toString();
	}
	
	public VariableStack getStack() {
		return stack;
	}
}
