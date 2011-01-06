// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.translator.atoms;

import java.io.IOException;
import java.io.Writer;

import org.globus.cog.karajan.parser.EvaluationContext;
import org.globus.cog.karajan.parser.EvaluationException;
import org.globus.cog.karajan.parser.atoms.Evaluator;
import org.globus.cog.karajan.translator.IndentationLevel;
import org.globus.cog.karajan.translator.TranslationContext;

public abstract class AbstractKarajanEvaluator implements Evaluator, KarajanEvaluator {

	public final Object evaluate(EvaluationContext variables) throws EvaluationException {
		try {
			Writer wr = (Writer) variables.get(TranslationContext.WRITER);
			IndentationLevel l = (IndentationLevel) variables.get(TranslationContext.INDENTATION);
			write(wr, l);
			return null;
		}
		catch (IOException e) {
			throw new EvaluationException(e);
		}
	}
}
