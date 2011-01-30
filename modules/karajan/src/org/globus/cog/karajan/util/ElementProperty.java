//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 2, 2005
 */
package org.globus.cog.karajan.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;

public abstract class ElementProperty {

	public abstract Object getValue(VariableStack stack) throws VariableNotFoundException;

	public static Object parse(final String value) throws ParsingException {
		StringBuffer sb = new StringBuffer();
		boolean var = false;
		LinkedList l = null;
		for (int i = 0; i < value.length(); i++) {
			final char c = value.charAt(i);
			if (c == '{') {
				if (i == value.length() - 1) {
					throw new ParsingException("Unmatched/unescaped '{': '" + value + "'");
				}
				if (value.charAt(i + 1) == '{') {
					sb.append('{');
					i++;
				}
				else {
					if (var) {
						throw new ParsingException("Unmatched/unescaped '{': '" + value + "'");
					}
					else {
						if (sb.length() > 0) {
							if (l == null) {
								l = new LinkedList();
							}
							l.add(new StringElement(sb.toString()));
							sb = new StringBuffer();
						}
						var = true;
					}
				}
			}
			else if (var && c == '}') {
				var = false;
				if (l == null) {
					l = new LinkedList();
				}
				l.add(new VariableElement(new Identifier(sb.toString())));
				sb = new StringBuffer();
			}
			else {
				sb.append(c);
			}
		}
		if (sb.length() != 0) {
			if (var) {
				throw new ParsingException("Missing '}': " + value);
			}
			if (l == null) {
				return sb.toString();
			}
			else {
				l.add(new StringElement(sb.toString()));
			}
		}
		if (l == null || l.size() == 0) {
			return "";
		}
		else if (l.size() == 1) {
			Object o = l.get(0);
			if (o instanceof StringElement) {
				System.err.println("Logic error: " + value);
				return ((StringElement) o).toString();
			}
			else {
				return o;
			}
		}
		else {
			return new Concatenator(l);
		}
	}

	public String getUnparsed() {
		return toString();
	}

	protected abstract String getValueAsString(VariableStack stack)
			throws VariableNotFoundException;

	private static final class StringElement extends ElementProperty {
		private final String str;

		public StringElement(String str) {
			this.str = str;
		}

		public Object getValue(VariableStack stack) {
			return str;
		}

		public String getValueAsString(VariableStack stack) {
			return str;
		}

		public String toString() {
			return str;
		}
	}

	private static final class VariableElement extends ElementProperty {
		private final String name;
		private int frame = -1;
		private Object value;

		public VariableElement(Identifier ident) {
			this.name = ident.getName();
		}

		public Object getValue(VariableStack stack) throws VariableNotFoundException {
			if (frame == -1 || frame == VariableStack.NO_FRAME) {
				frame = stack.getVarFrameFromTop(name);
				if (frame == VariableStack.NO_FRAME) {
					throw new VariableNotFoundException(name);
				}
				else if (frame == VariableStack.FIRST_FRAME) {
					value = stack.firstFrame().getVar(name);
					return value;
				}
				else {
					return stack.getFrameFromTop(frame).getVar(name);
				}
			}
			else if (frame == VariableStack.FIRST_FRAME) {
			    return value;
			}
			else {
				return stack.getFrameFromTop(frame).getVar(name);
			}
		}

		public String getValueAsString(VariableStack stack) throws VariableNotFoundException {
			return TypeUtil.toString(getValue(stack));
		}

		public String toString() {
			return '{' + name + '}';
		}
	}

	private static final class EmptyElement extends ElementProperty {
		public Object getValue(VariableStack stack) {
			return "";
		}

		public String getValueAsString(VariableStack stack) {
			return "";
		}

		public String toString() {
			return "";
		}
	}

	private static final class Concatenator extends ElementProperty {
		private final List<ElementProperty> l;

		public Concatenator(List<ElementProperty> l) {
			this.l = l;
		}

		public Object getValue(VariableStack stack) throws VariableNotFoundException {
			final StringBuilder sb = new StringBuilder();
			for (ElementProperty p : l) {
				sb.append(p.getValueAsString(stack));
			}
			return sb.toString();
		}

		public String getValueAsString(VariableStack stack) throws VariableNotFoundException {
			return (String) getValue(stack);
		}

		public String toString() {
			return l.toString();
		}

		public String getUnparsed() {
			StringBuffer sb = new StringBuffer();
			Iterator i = l.iterator();
			while (i.hasNext()) {
				sb.append(((ElementProperty) i.next()).getUnparsed());
			}
			return sb.toString();
		}
	}

}
