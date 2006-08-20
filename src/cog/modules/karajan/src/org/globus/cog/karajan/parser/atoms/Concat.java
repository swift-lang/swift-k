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

public class Concat extends AbstractAtom {

	public String toString() {
		return "CONCAT()";
	}

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		try {
			final Object o2 = stack.pop();
			final Object o1 = stack.pop();
			if (!(o1 instanceof Evaluator) && !(o2 instanceof Evaluator)) {
				stack.push(o1.toString() + o2.toString());
			}
			else if ("".equals(o1)) {
				stack.push(o2);
			}
			else if ("".equals(o2)) {
				stack.push(o1);
			}
			else if ((o1 instanceof Eval) && !(o2 instanceof Evaluator)
					&& !(((Eval) o1).getChild(1) instanceof Evaluator)) {
				stack.push(new Eval(((Eval) o1).getChild(0), ((Eval) o1).getChild(1).toString()
						+ o2.toString()));
			}
			else if ((o2 instanceof Eval) && !(o1 instanceof Evaluator)
					&& !(((Eval) o2).getChild(0) instanceof Evaluator)) {
				stack.push(new Eval(o1.toString() + ((Eval) o2).getChild(0).toString(),
						((Eval) o2).getChild(1)));
			}
			else {
				stack.push(new Eval(o1, o2));
			}
			return true;
		}
		catch (Exception e) {
			return true;
		}
	}

	private class Eval extends AbstractEvaluator {
		public Eval(Object o1, Object o2) {
			super(2);
			setChild(0, o1);
			setChild(1, o2);
		}

		public Object evaluate(EvaluationContext variables) throws EvaluationException {
			Object c1 = evalChild(0, variables);
			Object c2 = evalChild(1, variables);
			return c1.toString() + c2.toString();
		}

		public String toString() {
			return "CONCAT" + super.toString();
		}
	}
}