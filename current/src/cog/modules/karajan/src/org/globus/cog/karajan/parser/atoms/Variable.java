// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser.atoms;

import org.globus.cog.karajan.parser.EvaluationContext;
import org.globus.cog.karajan.parser.EvaluationException;
import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.Stack;
import org.globus.cog.karajan.translator.atoms.Identifier;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.Future;

public class Variable extends AbstractAtom {

	public boolean parse(ParserContext context, Stack stack) throws ParsingException {
		if (stack.isEmpty()) {
			throw new ParsingException("Empty stack");
		}
		Object o = stack.pop();
		if (o instanceof Identifier.Eval) {
			stack.push(new Eval(((Identifier.Eval) o).getValue()));
		}
		else {
			throw new ParsingException("Unexpected object on stack: " + o);
		}
		return true;
	}

	public static class Eval implements Evaluator {
		private final String name;

		public Eval(String name) {
			this.name = name;
		}

		public Object evaluate(EvaluationContext variables) throws EvaluationException {
			if (variables == null) {
				throw new EvaluationException(
						"Variable encountered, but no variable table supplied");
			}
			Object value = variables.get(name);

			if (value instanceof Future) {
				try {
					value = ((Future) value).getValue();
				}
				catch (ExecutionException e) {
					throw new EvaluationException(e);
				}
			}
			return value;
		}

		public String toString() {
			return "IDENTIFIER(" + name + ")";
		}
	}
}