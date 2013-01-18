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

public class ListAdd extends AbstractAtom {

	protected void setParams(String[] params) {
		assertEquals(params.length, 0, getClass());
	}

	public boolean parse(ParserContext context, Stack stack) throws ParsingException {
		Object obj = stack.pop();
		Object list = stack.peek();
		if (!(list instanceof java.util.List)) {
			throw new ParsingException("LISTADD() (List Object) called but stack was ("
					+ list + " " + obj + ")\n\tStack: "+stack.toString());

		}
		((java.util.List) list).add(obj);
		return true;
	}

	public String toString() {
		return "LISTADD()";
	}
}
