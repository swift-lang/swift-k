//----------------------------------------------------------------------
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


public class Error extends AbstractAtom {
	private String message;
	
	protected void setParams(String[] params) {
		assertEquals(params.length, 1, Error.class);
		message = params[0];
	}

	public boolean parse(ParserContext context, Stack stack) throws ParsingException {
		throw new ParsingException(message);
	}
	
	public String toString() {
		return "ERROR("+message+")";
	}
}
