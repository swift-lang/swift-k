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

public class Kernel extends AbstractAtom {
	private String name;

	protected void setParams(String[] params) {
		assertTrue(params.length == 1, Kernel.class);
		name = params[0];
	}

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		stack.push(new Eval(name, popstr(stack), context.tok.getLineNumber()));
		return true;
	}

	public static class Eval extends AbstractKarajanEvaluator {
		private final String value, name;
		private final int line;

		public Eval(String name, String value, int line) {
			this.value = value;
			this.line = line;
			this.name = name;
		}

		public void write(Writer wr, IndentationLevel l) throws IOException {
			l.write(wr);
			wr.write("<" + name + " _line=\"" + line + "\" file=\"");
			wr.write(value);
			wr.write("\"/>\n");
		}

		public String toString() {
			return "KERNEL(" + name + ", " + value + ")";
		}

		public String getValue() {
			return value;
		}
	}
}