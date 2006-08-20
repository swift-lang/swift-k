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
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.Stack;

public class NotLiteral extends AbstractAtom {
	private String value;

	protected void setParams(String[] params) {
		assertEquals(params.length, 1, NotLiteral.class);
		value = params[0].replaceAll("\\n", "\n").replaceAll("\\t", "\t");
	}

	public boolean parse(ParserContext context, Stack stack) throws ParsingException {
		if (context.tok.hasMoreTokens()) {
			if (context.tok.peekToken().equals(value)) {
				return false;
			}
			return true;
		}
		return false;
	}

	public String toString() {
		return "NOTLITERAL(" + value + ")";
	}
}
