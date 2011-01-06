// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 18, 2005
 */
package org.globus.cog.karajan.translator.atoms;

import java.io.IOException;
import java.io.Writer;

import org.globus.cog.karajan.parser.EvaluationException;
import org.globus.cog.karajan.translator.IndentationLevel;

public interface KarajanEvaluator {
	void write(Writer wr, IndentationLevel l) throws IOException, EvaluationException;
}
