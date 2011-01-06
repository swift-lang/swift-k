// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.translator.atoms;

import java.io.IOException;
import java.io.Writer;

import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.Stack;
import org.globus.cog.karajan.parser.atoms.AbstractAtom;
import org.globus.cog.karajan.translator.IndentationLevel;

public class Variable extends AbstractAtom {

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		final Object obj = stack.pop();
		String value;
		if (obj instanceof Identifier.Eval) {
			value = ((Identifier.Eval) obj).getValue();
		}
		else if (obj instanceof String) {
			value = (String) obj;
		}
		else {
			throw new ParsingException("Unexpected item on stack: " + obj);
		}
		stack.push(new Eval(value));
		return true;
	}

	public static class Eval extends AbstractKarajanEvaluator {
		private final String value;

		public Eval(String value) {
			this.value = value;
		}

		public void write(Writer wr, IndentationLevel l) throws IOException {
			l.write(wr);
			wr.write("<kernel:variable>");
			wr.write(value);
			wr.write("</kernel:variable>\n");
		}

		public String toString() {
			return "VARIABLE(" + value + ")";
		}

		public String getValue() {
			return value;
		}
	}
}