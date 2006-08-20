// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

public final class Rule extends AbstractGrammarElement {
	private String name;

	private GrammarElement or;

	public void read(PeekableEnumeration st, AtomMapping mapping) {
		name = (String) st.nextElement();
		expect(st, ":=");
		or = new Or();
		or.read(st, mapping);
	}

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		return or.parse(context, stack);
	}

	public Object getName() {
		return name;
	}

	public String toString() {
		Throwable t = new Throwable();
		StackTraceElement[] els = t.getStackTrace();
		boolean recursive = false;
		for (int i = 1; i < els.length; i++) {
			if (els[i].getClassName().equals(getClass().getName())) {
				recursive = true;
				break;
			}
		}
		if (recursive) {
			return name;
		}
		else {
			StringBuffer sb = new StringBuffer();
			sb.append(name);
			sb.append(" := ");
			sb.append(or.toString());
			return sb.toString();
		}
	}
	
	public String errorForm() {
		return or.errorForm();
	}

	public GrammarElement _optimize(Rules rules) {
		or = or.optimize(rules);
		return this;
	}

}