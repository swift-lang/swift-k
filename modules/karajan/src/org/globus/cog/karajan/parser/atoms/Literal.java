// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser.atoms;

import org.globus.cog.karajan.parser.Lexer;
import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.Stack;

public class Literal extends AbstractAtom {
	private String value;

	protected void setParams(String[] params) {
		assertEquals(params.length, 1, Literal.class);
		value = params[0];
	}

	public boolean parse(ParserContext context, Stack stack) {
		Lexer lexer = context.tok;
		Object mark = lexer.mark();
		for(int i = 0; i < value.length(); i++) {
			if (lexer.hasMoreChars()) {
				if (value.charAt(i) != lexer.nextChar()) {
					lexer.reset(mark);
					context.lastExpected = errorForm();
					return false;
				}
			}
			else {
				lexer.reset(mark);
				return false;
			}
		}
		return true;
	}

	public String toString() {
		return "LITERAL(" + value + ")";
	}

	public String errorForm() {
		return "'" + value + "'";
	}
}
