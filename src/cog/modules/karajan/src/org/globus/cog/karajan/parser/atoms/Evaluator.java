//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser.atoms;

import org.globus.cog.karajan.parser.EvaluationContext;
import org.globus.cog.karajan.parser.EvaluationException;

public interface Evaluator {

	Object evaluate(EvaluationContext variables) throws EvaluationException;
}
