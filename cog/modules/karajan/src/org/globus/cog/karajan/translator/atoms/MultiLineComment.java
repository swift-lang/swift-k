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

public class MultiLineComment extends SingleLineComment {

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		final StringBuffer sb = new StringBuffer();
		while (context.tok.hasMoreChars()) {
			char c = context.tok.nextChar();
			if (c != '*') {
				sb.append(c);
			}
			else if (context.tok.hasMoreTokens()) {
				c = context.tok.peekChar();
				if (c == '/') {
					context.tok.nextChar();
					stack.push(new Eval(sb.toString()));
					return true;
				}
				else {
					sb.append('*');
				}
			}
			else {
				throw new ParsingException("Unterminated comment");
			}
		}
		return true;
	}
}