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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.globus.cog.karajan.parser.EvaluationException;
import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.Stack;
import org.globus.cog.karajan.parser.atoms.AbstractAtom;
import org.globus.cog.karajan.translator.IndentationLevel;

public class EndElement extends AbstractAtom {

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		// they are pushed in reverse order
		final LinkedList children = new LinkedList();
		BeginElement.Eval begin;
		while (true) {
			if (stack.isEmpty()) {
				throw new ParsingException("Empty stack");
			}
			final Object next = stack.pop();
			if (next instanceof BeginElement.Eval) {
				begin = (BeginElement.Eval) next;
				break;
			}
			else {
				children.addFirst(next);
			}
		}
		stack.push(new Eval(begin, children));
		return true;
	}

	public String toString() {
		return "ENDELEMENT()";
	}

	public static class Eval extends AbstractKarajanEvaluator {
		private final List children;
		private final BeginElement.Eval begin;
		private Map properties;

		public Eval(BeginElement.Eval begin, List children) {
			this.children = children;
			this.begin = begin;
		}

		public void write(Writer wr, IndentationLevel l) throws IOException, EvaluationException {
			l.write(wr);
			wr.write("<");
			wr.write(begin.getName());
			if (properties != null) {
				Iterator i = properties.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry entry = (Map.Entry) i.next();
					wr.write(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
				}
			}
			wr.write(" _line=\"");
			wr.write(String.valueOf(begin.getLine()));
			wr.write("\"");
			if (children.size() == 0) {
				wr.write("/>\n");
			}
			else {
				Iterator i = children.iterator();
				wr.write(">\n");
				l.inc();
				while (i.hasNext()) {
					KarajanEvaluator child = (KarajanEvaluator) i.next();
					child.write(wr, l);
				}
				l.dec();
				l.write(wr);
				wr.write("</");
				wr.write(begin.getName());
				wr.write(">\n");
			}
		}
		
		public void setProperty(String name, String value) {
			if (properties == null) {
				properties = new Hashtable();
			}
			properties.put(name, value);
		}

		public String toString() {
			return "ELEMENT(" + begin.getName() + ")";
		}
	}
}