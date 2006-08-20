// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.parser.atoms.AbstractAtom;

public final class And extends AbstractGrammarElement {
	private static final Logger logger = Logger.getLogger(And.class);

	private final List elements;

	public And() {
		elements = new LinkedList();
	}

	public void read(final PeekableEnumeration st, final AtomMapping mapping) {
		while (true) {
			String next = (String) st.peek();
			if ("|".equals(next) || ";".equals(next)) {
				break;
			}
			if (Character.isLowerCase(next.charAt(0))) {
				UnresolvedRule ur = new UnresolvedRule();
				ur.read(st, null);
				elements.add(ur);
			}
			else {
				elements.add(AbstractAtom.newInstance(st, mapping));
			}
		}
	}

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		Iterator i = elements.iterator();
		final int mark = stack.mark();
		if (!i.hasNext()) {
			// empty rule always matches
			return true;
		}
		GrammarElement ge = (GrammarElement) i.next();
		if (!ge.parse(context, stack)) {
			stack.forget(mark);
			return false;
		}
		final int index = 0;
		while (i.hasNext()) {
			ge = (GrammarElement) i.next();
			try {
				if (!ge.parse(context, stack)) {
					debug(stack);
					throw new ParsingException(context.tok.currentLine() + "\nExpected "
							+ ge.errorForm() + " but got '" + context.tok.peekToken() + "'");
				}
			}
			catch (ParsingException e) {
				throw e;
			}
			catch (Exception e) {
				debug(stack);
				throw new ParsingException("Exception executing " + ge.toString());
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
		Iterator i = elements.iterator();
		while (i.hasNext()) {
			sb.append(i.next());
			sb.append(' ');
		}
		return sb.toString();
	}

	public String errorForm() {
		return ((GrammarElement) elements.get(0)).errorForm();
	}

	public GrammarElement _optimize(Rules rules) {
		if (elements.size() == 1) {
			return ((GrammarElement) elements.get(0)).optimize(rules);
		}
		for (int i = 0; i < elements.size(); i++) {
			elements.set(i, ((GrammarElement) elements.get(i)).optimize(rules));
		}
		return this;
	}
}