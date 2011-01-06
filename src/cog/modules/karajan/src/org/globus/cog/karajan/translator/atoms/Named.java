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

import org.globus.cog.karajan.parser.EvaluationException;
import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.Stack;
import org.globus.cog.karajan.parser.atoms.AbstractAtom;
import org.globus.cog.karajan.translator.IndentationLevel;
import org.globus.cog.karajan.util.serialization.XMLUtils;

public class Named extends AbstractAtom {

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		final Object o = stack.pop();
		if (!(o instanceof KarajanEvaluator)) {
			throw new ParsingException("Invalid object on stack: " + o);
		}
		KarajanEvaluator value = (KarajanEvaluator) o;
		Identifier.Eval name = (Identifier.Eval) stack.pop();
		stack.push(new Eval(name, value));
		return true;
	}

	public static class Eval extends AbstractKarajanEvaluator {
		private final KarajanEvaluator value;
		private final Identifier.Eval key;

		public Eval(Identifier.Eval key, KarajanEvaluator value) {
			this.value = value;
			this.key = key;
		}

		public void write(Writer wr, IndentationLevel l) throws IOException, EvaluationException {
			l.write(wr);
			wr.write("<kernel:named name=\"");
			wr.write(key.getValue());
			wr.write("\"");
			if (value instanceof Identifier.Eval) {
				wr.write(" value=\"");
				wr.write(((Identifier.Eval) value).getValue());
				wr.write("\"/>\n");
			}
			else if (value instanceof StringValue.Eval) {
				wr.write(" value=\"");
				wr.write(XMLUtils.escape(((StringValue.Eval) value).getValue()));
				wr.write("\"/>\n");
			}
			else if (value instanceof Variable.Eval) {
				wr.write(" value=\"{");
				wr.write(((Variable.Eval) value).getValue());
				wr.write("}\"/>\n");
			}
			else {
				wr.write(">\n");
				l.inc();
				value.write(wr, l);
				l.dec();
				l.write(wr);
				wr.write("</kernel:named>\n");
			}
		}

		public String toString() {
			return "NAMED(" + key + ", " + value + ")";
		}
	}
}