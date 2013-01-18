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

public class ElementDef extends AbstractAtom {

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		String cls = popstr(stack);
		Identifier.Eval name = (Identifier.Eval) stack.pop();
		stack.push(new Eval(name.getValue(), cls));
		return true;
	}

	public static class Eval extends AbstractKarajanEvaluator {
		private final String name, cls;

		public Eval(String name, String cls) {
			this.name = name;
			this.cls = cls;
		}

		public void write(Writer wr, IndentationLevel l) throws IOException {
			l.write(wr);
			wr.write("<kernel:elementDef type=\"" + name + "\" className=\"" + cls + "\"/>\n");
		}

		public String toString() {
			return "ELEMENTDEF(" + name + ", " + cls + ")";
		}
	}
}