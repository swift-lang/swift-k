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

public final class Or extends AbstractGrammarElement {
	private final List ands;

	public Or() {
		ands = new LinkedList();
	}

	public void read(PeekableEnumeration st, AtomMapping mapping) {
		while (true) {
			And and = new And();
			and.read(st, mapping);
			ands.add(and);
			if (st.peek().equals(";")) {
				break;
			}
			expect(st, "|");
		}
		expect(st, ";");
	}

	public boolean parse(ParserContext context, Stack stack) throws ParsingException {
		Iterator i = ands.iterator();
		while (i.hasNext()) {
			GrammarElement and = (GrammarElement) i.next();
			int mark = stack.mark();
			if (and.parse(context, stack)) {
				return true;
			}
			stack.forget(mark);
		}
		return false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator i = ands.iterator();
		boolean first = true;
		while (i.hasNext()) {
			if (first) {
				first = false;
			}
			else {
				sb.append(" |\n");
			}
			sb.append(i.next());
		}
		sb.append(" ;\n");
		return sb.toString();
	}
	
	public String errorForm() {
		StringBuffer sb = new StringBuffer();
		Iterator i = ands.iterator();
		boolean first = true;
		while (i.hasNext()) {
			if (first) {
				first = false;
			}
			else {
				sb.append(" or ");
			}
			sb.append(((GrammarElement) i.next()).errorForm());
		}
		return sb.toString();
	}

	public GrammarElement _optimize(Rules rules) {
		if (ands.size() == 1) {
			return ((GrammarElement) ands.get(0)).optimize(rules);
		}
		for (int i = 0; i < ands.size(); i++) {
			ands.set(i, ((GrammarElement) ands.get(i)).optimize(rules));
		}
		return this;
	}
}