//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 14, 2005
 */
package org.globus.cog.karajan.translator;

import java.io.CharArrayWriter;

import org.globus.cog.karajan.parser.EvaluationContext;
import org.globus.cog.karajan.parser.UndefinedVariableException;

public class TranslationContext implements EvaluationContext {
	public static final String WRITER = "writer";
	public static final String INDENTATION = "indentation";
	
	private final CharArrayWriter writer;
	private final IndentationLevel indentation;
	
	public TranslationContext() {
		writer = new CharArrayWriter();
		indentation = new IndentationLevel();
	}

	public Object get(String name) throws UndefinedVariableException {
		if (name.equals(WRITER)) {
			return writer;
		}
		else if (name.equals(INDENTATION)) {
			return indentation;
		}
		else {
			return null;
		}
	}

	public boolean hasVariable(String name) {
		return name.equals(WRITER) || name.equals(INDENTATION);
	}
	
	public CharArrayWriter getWriter() {
		return writer;
	}
}
