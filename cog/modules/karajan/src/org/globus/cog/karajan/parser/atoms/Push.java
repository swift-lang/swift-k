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


public class Push extends AbstractAtom {
	private String value;
	
	protected void setParams(String[] params) {
		assertEquals(params.length, 1, Push.class);
		value = params[0];
	}

	public boolean parse(ParserContext context, Stack stack) {
		stack.push(value);
		return true;
	}
	
	public String toString() {
		return "PUSH("+value+")";
	}
	
	public String errorForm() {
		return "'"+value+"'";
	}
}
