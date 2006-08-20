//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ParserContext {
	public Lexer tok;
	public AtomMapping mapping;
	public Rules grammar;
	public LinkedList queue;
	public String lastExpected;
	public Map data = new HashMap();
	public ParseTree tree;
}
