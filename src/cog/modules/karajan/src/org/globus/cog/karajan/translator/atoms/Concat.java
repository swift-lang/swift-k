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
import java.util.ArrayList;
import java.util.Arrays;

import org.globus.cog.karajan.parser.EvaluationContext;
import org.globus.cog.karajan.parser.EvaluationException;
import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.Stack;
import org.globus.cog.karajan.parser.atoms.AbstractAtom;
import org.globus.cog.karajan.parser.atoms.AbstractEvaluator;
import org.globus.cog.karajan.translator.IndentationLevel;
import org.globus.cog.karajan.util.TypeUtil;

public class Concat extends AbstractAtom {

	public String toString() {
		return "CONCAT()";
	}

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		try {
			final Object o2 = stack.pop();
			final Object o1 = stack.pop();
			String value1 = null;
			String value2 = null;
			if (o1 instanceof StringValue.Eval) {
				value1 = ((StringValue.Eval) o1).getValue();
			}
			else if (o1 instanceof String) {
				value1 = (String) o1;
			}
			if (o2 instanceof StringValue.Eval) {
				value2 = ((StringValue.Eval) o2).getValue();
			}
			else if (o2 instanceof String) {
				value2 = (String) o2;
			}

			if (value1 != null && value2 != null) {
				stack.push(new StringValue.Eval(value1 + value2));
			}
			else if ("".equals(value1)) {
				stack.push(o2);
			}
			else if ("".equals(value2)) {
				stack.push(o1);
			}
			else if (o1 instanceof Eval && o2 instanceof Eval) {
				stack.push(new Eval(((Eval) o1).getChildren(), ((Eval) o2).getChildren()));
			}
			else if (o1 instanceof Eval) {
				stack.push(new Eval(((Eval) o1).getChildren(), new Object[] { o2 }));
			}
			else if (o2 instanceof Eval) {
				stack.push(new Eval(new Object[] { o1 }, ((Eval) o2).getChildren()));
			}
			else {
				if (value1 != null) {
					stack.push(new Eval(new StringValue.Eval(value1), o2));
				}
				else if (value2 != null) {
					stack.push(new Eval(o1, new StringValue.Eval(value2)));
				}
				else {
					stack.push(new Eval(o1, o2));
				}
			}
			return true;
		}
		catch (Exception e) {
			throw new ParsingException(e);
		}
	}

	private static class Eval extends AbstractEvaluator implements KarajanEvaluator {
		public Eval(final Object o1, final Object o2) {
			super();
			//interesting. Although o1 and o2 are known to be arrays at compile
			//time, the compiler still points to this constructor
			if (o1.getClass().isArray()) {
				setChildren((Object[]) o1, (Object[]) o2);
			}
			else {
				initializeChildren(2);
				setChild(0, o1);
				setChild(1, o2);
			}
		}

		public Eval(Object[] a1, Object[] a2) {
			super();
			setChildren(a1, a2);
		}

		protected void setChildren(Object[] a1, Object[] a2) {
			ArrayList l = new ArrayList();
			l.addAll(Arrays.asList(a1));
			l.addAll(Arrays.asList(a2));
			int index = 0;
			while (index + 1 < l.size()) {
				Object o1 = l.get(index);
				Object o2 = l.get(index + 1);
				if (isString(o1) && isString(o2)) {
					l.set(index, getString(o1) + getString(o2));
					l.remove(index + 1);
				}
				else {
					index++;
				}
			}
			super.initializeChildren(l.size());
			for (int i = 0; i < l.size(); i++) {
				Object o = l.get(i);
				if (o instanceof KarajanEvaluator) {
					setChild(i, o);
				}
				else if (o instanceof String) {
					setChild(i, new StringValue.Eval((String) o));
				}
				else {
					throw new RuntimeException("Unexpected object " + o);
				}
			}
		}

		protected Object[] getChildren() {
			return super.getChildren();
		}

		public boolean isString(int index) {
			return isString(getChild(index));
		}

		private boolean isString(Object obj) {
			return obj instanceof StringValue.Eval || obj instanceof String;
		}

		public String getString(int index) {
			return getString(getChild(index));
		}

		public String getString(Object o) {
			if (o instanceof String) {
				return (String) o;
			}
			else if (o instanceof StringValue.Eval) {
				return ((StringValue.Eval) o).getValue();
			}
			else {
				throw new RuntimeException("Child " + o + " is not a string");
			}
		}

		public Object evaluate(EvaluationContext variables) throws EvaluationException {
			Object c1 = evalChild(0, variables);
			Object c2 = evalChild(1, variables);
			return TypeUtil.toString(c1) + TypeUtil.toString(c2);
		}

		public String toString() {
			return "CONCAT" + super.toString();
		}

		public void write(Writer wr, IndentationLevel l) throws IOException, EvaluationException {
			l.write(wr);
			wr.write("<sys:concat>\n");
			l.inc();
			for (int i = 0; i < childCount(); i++) {
				Object child = getChild(i);
				if (child instanceof KarajanEvaluator) {
					((KarajanEvaluator) child).write(wr, l);
				}
				else {
					throw new RuntimeException("Unexpected object " + child);
				}
			}
			l.dec();
			l.write(wr);
			wr.write("</sys:concat>\n");
		}
	}
}