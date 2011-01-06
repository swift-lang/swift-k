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

public class HorizontalWhitespace extends AbstractAtom {

	protected void setParams(String[] params) {
		assertEquals(params.length, 0, HorizontalWhitespace.class);
	}

	public boolean parse(final ParserContext context, final Stack stack) {
		while (context.tok.hasMoreChars()
				&& (context.tok.peekChar() == ' ' || context.tok.peekChar() == '\t')) {
			context.tok.nextChar();
		}
		return true;
	}

	public String toString() {
		return "HSPACES()";
	}
}
