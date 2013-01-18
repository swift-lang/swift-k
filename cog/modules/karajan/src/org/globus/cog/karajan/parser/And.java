// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.parser.atoms.AbstractAtom;

public final class And extends AbstractGrammarElement {
	private static final Logger logger = Logger.getLogger(And.class);

	private GrammarElement[] elements;

	public And() {
	}

	public void read(final PeekableEnumeration st, final AtomMapping mapping) {
		ArrayList e = new ArrayList();
		while (true) {
			String next = (String) st.peek();
			if ("|".equals(next) || ";".equals(next)) {
				break;
			}
			if (Character.isLowerCase(next.charAt(0))) {
				UnresolvedRule ur = new UnresolvedRule();
				ur.read(st, null);
				e.add(ur);
			}
			else {
				e.add(AbstractAtom.newInstance(st, mapping));
			}
		}
		elements = (GrammarElement[]) e.toArray(GEATYPE);
	}

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		if (elements.length == 0) {
			// empty rule always matches
			return true;
		}
		final int mark = stack.mark();
		if (!elements[0].parse(context, stack)) {
			stack.forget(mark);
			return false;
		}
		final int index = 0;
		for (int i = 1; i < elements.length; i++) {
			GrammarElement ge = elements[i];
			try {
				if (!ge.parse(context, stack)) {
					debug(stack);
					throw new ParsingException("Line " + context.tok.getLineNumber() + ": "
							+ context.tok.currentLine() + "\nExpected " + ge.errorForm()
							+ " but got '" + context.tok.peekToken() + "'");
				}
			}
			catch (ParsingException e) {
				throw e;
			}
			catch (Exception e) {
				debug(stack);
				throw new ParsingException("Exception executing " + ge.toString(), e);
			}
		}
		return true;
	}

	private void debug(Stack stack) {
		if (logger.isDebugEnabled()) {
			logger.debug("Rule: " + toString());
			logger.debug("Parse stack: " + stack);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < elements.length; i++) {
			sb.append(elements[i]);
			sb.append(' ');
		}
		return sb.toString();
	}

	public String errorForm() {
		return elements[0].errorForm();
	}

	public GrammarElement _optimize(Rules rules) {
		if (elements.length == 1) {
			return elements[0].optimize(rules);
		}
		for (int i = 0; i < elements.length; i++) {
			elements[i] = elements[i].optimize(rules);
		}
		return this;
	}
}