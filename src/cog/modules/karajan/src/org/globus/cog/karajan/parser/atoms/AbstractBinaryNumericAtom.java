//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 21, 2005
 */
package org.globus.cog.karajan.parser.atoms;

import org.globus.cog.karajan.parser.ParserContext;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.Stack;

public abstract class AbstractBinaryNumericAtom extends AbstractAtom {
	protected AbstractBinaryNumericEvaluator staticEvaluator;
	
	public AbstractBinaryNumericAtom() {
		staticEvaluator = newEval(null, null);
	}

	public boolean parse(final ParserContext context, final Stack stack) throws ParsingException {
		final Object v1 = stack.pop();
		final Object v2 = stack.pop();
		if ((v1 instanceof Number) && (v2 instanceof Number)) {
			stack.push(staticEvaluator.compute((Number) v1, (Number) v2));
		}
		else {
			stack.push(newEval(v1, v2));
		}
		return true;
	}
	
	protected abstract AbstractBinaryNumericEvaluator newEval(Object v1, Object v2);
}
