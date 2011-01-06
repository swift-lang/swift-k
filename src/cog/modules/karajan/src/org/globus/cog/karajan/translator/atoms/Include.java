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

public class Include extends AbstractAtom {

	public boolean parse(ParserContext context, Stack stack) throws ParsingException {
		stack.push(new Eval(popstr(stack), context.tok.getLineNumber()));
		return true;
	}

	public static class Eval extends AbstractKarajanEvaluator {
		private final String value;
		private final int line;

		public Eval(String value, int line) {
			this.value = value;
			this.line = line;
		}

		public void write(Writer wr, IndentationLevel l) throws IOException {
			l.write(wr);
			wr.write("<include _line=\""+line+"\" file=\"");
			wr.write(value);
			wr.write("\"/>\n");
		}

		public String toString() {
			return "INCLUDE(" + value + ")";
		}

		public String getValue() {
			return value;
		}
	}
}