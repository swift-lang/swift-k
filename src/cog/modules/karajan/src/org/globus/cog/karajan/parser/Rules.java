// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Rules extends AbstractGrammarElement {
	private final Map rules;
	private Rule root;

	public Rules() {
		rules = new HashMap();
	}

	public void read(PeekableEnumeration st, AtomMapping mapping) {
		while (st.hasMoreElements()) {
			Rule rule = new Rule();
			rule.read(st, mapping);
			if (root == null) {
				root = rule;
			}
			rules.put(rule.getName(), rule);
		}
	}

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		return root.parse(context, stack);
	}

	public GrammarElement getRule(final String name) {
		return (GrammarElement) rules.get(name);
	}

	public GrammarElement _optimize(Rules rules) {
		Iterator i = this.rules.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			entry.setValue(((GrammarElement) entry.getValue()).optimize(rules));
		}
		return this;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator i = rules.values().iterator();
		while (i.hasNext()) {
			sb.append(i.next());
			sb.append('\n');
		}
		return sb.toString();
	}

}