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


public class Rot extends AbstractAtom {
	
	protected void setParams(String[] params) {
		assertEquals(params.length, 0, Rot.class);
	}

	public boolean parse(ParserContext context, Stack stack) {
		Object o1 = stack.pop();
		Object o2 = stack.pop();
		Object o3 = stack.pop();
		stack.push(o1);
		stack.push(o3);
		stack.push(o2);
		return true;
	}
	
	public String toString() {
		return "ROT()";
	}
}
