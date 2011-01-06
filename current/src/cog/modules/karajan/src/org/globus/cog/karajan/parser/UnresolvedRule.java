//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

public class UnresolvedRule extends AbstractGrammarElement {
	private String name;
	private GrammarElement ref;

	public void read(PeekableEnumeration st, AtomMapping mapping) {
		name = (String) st.nextElement();
	}

	public boolean parse(ParserContext context, Stack stack) throws ParsingException {
		if (ref == null) {
			ref = context.grammar.getRule(name);
			if (ref == null) {
				throw new GrammarException("Undefined rule: "+name);
			}
		}
		return ref.parse(context, stack);
	}

	public String toString() {
		return name;
	}
	
	public GrammarElement _optimize(Rules rules) {
		ref = rules.getRule(name);
		if (ref == null) {
			throw new GrammarException("Undefined rule: "+name);
		}
		return ref;
	}
}
