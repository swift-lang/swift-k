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

public class Transliterate extends AbstractAtom {

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		final Object obj = stack.pop();
		if (obj instanceof Identifier.Eval) {
			final Identifier.Eval value = (Identifier.Eval) obj;
			stack.push(new Identifier.Eval(Transliterator.transliterate(value.getValue())));
		}
		else {
			throw new ParsingException(this + ": Unexpected item on stack: " + obj);
		}
		return true;
	}
	
	public String toString() {
		return "TRANSLITERATE()";
	}
}