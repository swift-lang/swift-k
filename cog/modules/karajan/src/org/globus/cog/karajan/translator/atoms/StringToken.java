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

public class StringToken extends AbstractAtom {

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		final StringBuffer sb = new StringBuffer();
		boolean once = false;
		while (context.tok.hasMoreChars()) {
			final char c = context.tok.peekChar();
			if (c != '"' && c != '{') {
				sb.append(context.tok.nextChar());
				once = true;
			}
			else {
				break;
			}
		}
		if (once) {
			stack.push(new StringValue.Eval(sb.toString()));
			return true;
		}
		else {
			return false;
		}

	}

	public String errorForm() {
		return "STRINGTOKEN";
	}
}