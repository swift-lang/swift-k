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
import java.util.List;

public final class Or extends AbstractGrammarElement {
	private GrammarElement[] ands;

	public Or() {
	}

	public void read(PeekableEnumeration st, AtomMapping mapping) {
		List a = new ArrayList();
		while (true) {
			And and = new And();
			and.read(st, mapping);
			a.add(and);
			if (st.peek().equals(";")) {
				ands = (GrammarElement[]) a.toArray(GEATYPE);
				break;
			}
			expect(st, "|");
		}
		expect(st, ";");
	}

	public boolean parse(ParserContext context, Stack stack) throws ParsingException {
		for (int i = 0; i < ands.length; i++) {
			int mark = stack.mark();
			if (ands[i].parse(context, stack)) {
				return true;
			}
			stack.forget(mark);
		}
		return false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (int i = 0; i < ands.length; i++) {
			if (first) {
				first = false;
			}
			else {
				sb.append(" |\n");
			}
			sb.append(ands[i]);
		}
		sb.append(" ;\n");
		return sb.toString();
	}
	
	public String errorForm() {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for(int i = 0; i < ands.length; i++) {
			if (first) {
				first = false;
			}
			else {
				sb.append(" or ");
			}
			sb.append(ands[i].errorForm());
		}
		return sb.toString();
	}

	public GrammarElement _optimize(Rules rules) {
		if (ands.length == 1) {
			return ands[0].optimize(rules);
		}
		for (int i = 0; i < ands.length; i++) {
			ands[i] = ands[i].optimize(rules);
		}
		return this;
	}
}