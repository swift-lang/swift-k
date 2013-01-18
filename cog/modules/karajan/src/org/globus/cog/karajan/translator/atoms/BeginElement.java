// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.translator.atoms;

import org.globus.cog.karajan.parser.EvaluationContext;
import org.globus.cog.karajan.parser.EvaluationException;
import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.Stack;
import org.globus.cog.karajan.parser.atoms.AbstractAtom;
import org.globus.cog.karajan.parser.atoms.Evaluator;

public class BeginElement extends AbstractAtom {

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		final Object obj = stack.pop();
		if (!(obj instanceof Identifier.Eval)) {
			throw new ParsingException("Expected identifier, got "+obj);
		}
		String name = ((Identifier.Eval) obj).getValue();
		if (name == null) {
			throw new ParsingException("Null element name!");
		}
		if (name.equals("")) {
			throw new ParsingException("Empty element name");
		}
		stack.push(new Eval(name, context.tok.getLineNumber()));
		return true;
	}
	
	public String toString() {
		return "BEGINELEMENT()";
	}

	public static class Eval implements Evaluator {
		private final String name;
		private final int line;

		public Eval(String name, int line) {
			this.line = line;
			this.name = name;
			int index = 0;
		}

		public Object evaluate(EvaluationContext variables) throws EvaluationException {
			return null;
		}

		public String toString() {
			return "BEGINELEMENT(" + name + ")";
		}

		public String getName() {
			return name;
		}

		public int getLine() {
			return line;
		}
	}
}