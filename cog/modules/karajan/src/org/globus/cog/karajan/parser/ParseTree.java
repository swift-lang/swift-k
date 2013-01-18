// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

import org.globus.cog.karajan.parser.atoms.Evaluator;

public class ParseTree {
	private final Object root;
	private final Stack stack;
	private String unparsed;

	public ParseTree(Stack stack) {
		this.stack = stack;
		if (stack.size() != 1) {
			throw new GrammarException("Invalid expression: " + this+". Stack is "+stack);
		}
		else {
			root = stack.pop();
		}
	}

	public Object execute(EvaluationContext variables) throws EvaluationException {
		try {
			if (root instanceof Evaluator) {
				return ((Evaluator) root).evaluate(variables);
			}
			else {
				return root;
			}
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new EvaluationException(e.getMessage()
					+ " while evaluating expression " + this, e);
		}
	}

	public String toString() {
		return unparsed;
	}

	public void setUnparsed(String expr) {
		this.unparsed = expr;
	}

	public String getUnparsed() {
		return unparsed;
	}

	public boolean isConstant() {
		return !(root instanceof Evaluator);
	}

	public Object reduced() {
		if (isConstant()) {
			return root;
		}
		else {
			return this;
		}
	}
	
	public String treeString() {
		return root.toString();
	}
	
	public Stack getStack() {
		return stack;
	}
}