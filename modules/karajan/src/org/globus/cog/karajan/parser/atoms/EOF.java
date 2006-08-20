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
import org.globus.cog.karajan.parser.Stack;


public class EOF extends AbstractAtom {

	protected void setParams(String[] params) {
		assertEquals(params.length, 0, getClass());
	}

	public boolean parse(final ParserContext context, final Stack stack) {
		if (context.tok.hasMoreTokens()) {
			return false;
		}
		return true;
	}
	
	public String toString() {
		return "EOF()";
	}
}
