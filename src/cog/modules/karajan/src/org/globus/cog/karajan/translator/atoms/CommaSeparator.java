//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 14, 2005
 */
package org.globus.cog.karajan.translator.atoms;

import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.Stack;
import org.globus.cog.karajan.parser.atoms.AbstractAtom;

public class CommaSeparator extends AbstractAtom {

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		boolean comma = false;
		while(context.tok.hasMoreChars()) {
			final char c = context.tok.peekChar();
			if (c == ',') {
				comma = true;
			}
			else if (!Character.isWhitespace(c)) {
				return comma;
			}
			context.tok.nextChar();
		}
		return false;
	}
}
