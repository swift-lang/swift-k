// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

import java.util.Enumeration;

public abstract class AbstractGrammarElement implements GrammarElement {
	public static final GrammarElement[] GEATYPE = new GrammarElement[0];
	protected boolean optimized;

	public void expect(Enumeration st, String expected) {
		String found = (String) st.nextElement();
		if (!expected.equals(found)) {
			throw new GrammarException("Invalid gramar definition. Expected " + expected + ", got "
					+ found);
		}
	}
	
	public static void assertTrue(boolean value, Class c) {
		assertTrue(value, c.getName());
	}

	public static void assertTrue(boolean value, String message) {
		if (value == false) {
			throw new GrammarException("Assertion failed for " + message, new Throwable());
		}
	}

	public static void assertFalse(boolean value) {
		if (value == true) {
			throw new GrammarException("Assertion failed", new Throwable());
		}
	}

	public static void assertEquals(final int v1, final int v2, final Class c) {
		if (v1 != v2) {
			throw new GrammarException("Assertion failed in class " + c.getName() + ": " + v1
					+ " = " + v2, new Exception());
		}
	}

	public GrammarElement optimize(Rules rules) {
		if (!optimized) {
			optimized = true;
			return _optimize(rules);
		}
		return this;
	}

	public abstract GrammarElement _optimize(Rules rules);

	public String errorForm() {
		return toString();
	}
}