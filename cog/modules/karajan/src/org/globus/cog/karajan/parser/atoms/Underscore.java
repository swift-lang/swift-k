// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser.atoms;

import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.Stack;

public class Underscore extends AbstractAtom {
	private boolean push;
	
	protected void setParams(String[] params) {
		if (params.length != 0) {
			assertEquals(params.length, 1, Underscore.class);
			assertTrue(params[0].equals("push"), getClass());
			push = true;
		}
	}

	public boolean parse(ParserContext context, Stack stack) {
		if (context.tok.hasMoreTokens()) {
			char c = context.tok.peekChar();
			if (c == '_') {
				if (push) {
					stack.push("_");
				}
				context.tok.nextChar();
				return true;
			}
		}
		return false;
	}

	public String toString() {
		return "UNDERSCORE()";
	}
}