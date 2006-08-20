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

public class Identifier extends AbstractAtom {
	private String value;

	private static boolean[] excluded = new boolean[128];

	private static void excl(final char[] c) {
		for (int i = 0; i < c.length; i++) {
			if (c[i] < 0 || c[i] > 127) {
				throw new RuntimeException("Invalid excluded character: " + c[i]);
			}
			excluded[c[i]] = true;
		}
	}

	private static boolean isExcluded(final char c) {
		if (c < 0 || c > 127) {
			return false;
		}
		return excluded[c];
	}

	static {
		excl(new char[] { '(', ')', '[', ']', '{', '}', '+', '-', '/', '*', '%', '^', '&', '|',
				'=', '!', '"', ',' });
	}

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		if (value != null) {
			stack.push(new Eval(value));
			return true;
		}
		else {
			final StringBuffer sb = new StringBuffer();
			boolean once = false;
			char first = 0;
			while (context.tok.hasMoreChars()) {
				final char c = context.tok.peekChar();
				if (Character.isLetterOrDigit(c) || (!Character.isWhitespace(c) && !isExcluded(c))) {
					if (c == '=') {
						if (!once) {
							final char cc = context.tok.peekNextChar();
							if (cc == '=') {
								context.tok.nextChar();
								context.tok.nextChar();
								stack.push(new Eval("=="));
							}

							return true;
						}
						else if (sb.length() == 1) {
							if (Character.isLetterOrDigit(first)) {
								break;
							}
							else {
								sb.append(context.tok.nextChar());
								break;
							}
						}
						else {
							break;
						}
					}
					sb.append(context.tok.nextChar());
					if (!once) {
						first = c;
					}
					once = true;
				}
				else {
					break;
				}
			}
			if (once) {
				stack.push(new Eval(sb.toString().toLowerCase()));
				return true;
			}
			else {
				return false;
			}
		}
	}

	protected void setParams(String[] params) {
		if (params.length != 0) {
			assertEquals(params.length, 1, getClass());
			value = params[0];
		}
	}

	public String errorForm() {
		return "IDENTIFIER";
	}

	public static class Eval extends AbstractKarajanEvaluator {
		private final String value;

		public Eval(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public String toString() {
			return "IDENTIFIER(" + value + ")";
		}

		public void write(Writer wr, IndentationLevel l) throws IOException, EvaluationException {
			l.write(wr);
			wr.write("<identifier>");
			wr.write(value);
			wr.write("</identifier>\n");
		}
	}
}