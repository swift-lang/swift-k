//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;


public interface GrammarElement {
	void read(PeekableEnumeration st, AtomMapping mapping);
	
	boolean parse(ParserContext context, Stack stack) throws ParsingException;
	
	GrammarElement optimize(Rules rules);
	
	String errorForm();
}
