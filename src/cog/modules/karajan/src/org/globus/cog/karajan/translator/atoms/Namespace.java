// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.translator.atoms;

import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.Stack;
import org.globus.cog.karajan.parser.atoms.AbstractAtom;

public class Namespace extends AbstractAtom {
	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		final EndElement.Eval ns = (EndElement.Eval) stack.pop();
		final String prefix = popstr(stack);
		ns.setProperty("prefix", prefix);
		stack.push(ns);
		return true;
	}
}