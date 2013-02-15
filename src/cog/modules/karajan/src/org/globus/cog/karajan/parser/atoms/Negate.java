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

public class Negate extends AbstractAtom {

	protected void setParams(String[] params) {
		assertEquals(params.length, 0, Negate.class);
	}

	public boolean parse(ParserContext context, Stack stack) throws ParsingException {
		try {
			String f = (String) stack.pop();
			stack.push("-" + f);
		}
		catch (Exception e) {
			throw new ParsingException("Invalid value on stack ", e);
		}

		return true;
	}

	public String toString() {
		return "NEG()";
	}
}