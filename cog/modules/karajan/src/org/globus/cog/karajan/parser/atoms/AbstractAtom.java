// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser.atoms;

import org.globus.cog.karajan.parser.AbstractGrammarElement;
import org.globus.cog.karajan.parser.AtomMapping;
import org.globus.cog.karajan.parser.EvaluationContext;
import org.globus.cog.karajan.parser.EvaluationException;
import org.globus.cog.karajan.parser.GrammarElement;
import org.globus.cog.karajan.parser.GrammarException;
import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.PeekableEnumeration;
import org.globus.cog.karajan.parser.Rules;
import org.globus.cog.karajan.parser.Stack;
import org.globus.cog.karajan.translator.atoms.StringValue;

public abstract class AbstractAtom extends AbstractGrammarElement {
	public static AbstractAtom newInstance(final PeekableEnumeration st, final AtomMapping mapping) {
		final String atomStr = (String) st.nextElement();
		int index = atomStr.indexOf('(');
		assertTrue(index != -1, atomStr);
		final String name = atomStr.substring(0, index);
		assertFalse(name.endsWith("("));
		final String allParams = atomStr.substring(index + 1, atomStr.length() - 1);

		final ParamTokenizer stp = new ParamTokenizer(allParams);
		final String[] params = new String[stp.countTokens()];
		if (params.length != 0) {
			for (int i = 0; i < params.length; i++) {
				params[i] = stp.nextToken().replace('_', ' ');
			}
		}

		final Class cls = mapping.get(name);
		if (cls == null) {
			throw new GrammarException("Unknown atom: " + name);
		}
		try {
			AbstractAtom atom = (AbstractAtom) cls.newInstance();
			atom.setParams(params);
			return atom;
		}
		catch (Exception e) {
			throw new GrammarException(atomStr, e);
		}
	}

	protected void setParams(String[] params) {

	}

	public final void read(PeekableEnumeration st, AtomMapping mapping) {
		throw new GrammarException("Should not read an atom");
	}

	public GrammarElement _optimize(final Rules rules) {
		return this;
	}

	public boolean parse(ParserContext context, Stack stack) throws ParsingException {
		System.err.println("default parse() called for " + this.getClass());
		return false;
	}

	public Object evaluate(EvaluationContext variables) throws EvaluationException {
		throw new EvaluationException(toString() + " cannot be evaluated.");
	}

	private final static class ParamTokenizer {
		private final String str;
		private int crt;

		public ParamTokenizer(String str) {
			this.str = str;
			crt = 1;
		}

		public int countTokens() {
			if (str.length() == 0) {
				return 0;
			}
			int last = 1;
			int count = 1;
			do {
				last = str.indexOf("\",", last);
				if (last != -1) {
					count++;
				}
			}
			while (last != -1);
			return count;
		}

		public String nextToken() {
			final int next = str.indexOf("\",", crt);
			String ns;
			if (next == -1) {
				ns = str.substring(crt, str.length() - 1);
			}
			else {
				ns = str.substring(crt, next);
			}
			crt = next + 2;
			return ns;
		}
	}
	
	protected String popstr(final Stack stack) throws ParsingException {
		final Object obj = stack.pop();
		if (obj instanceof String) {
			return (String) obj;
		}
		else if (obj instanceof StringValue.Eval) {
			return ((StringValue.Eval) obj).getValue();
		}
		else {
			throw new ParsingException("Unexpected item on stack: " + obj);
		}
	}
}