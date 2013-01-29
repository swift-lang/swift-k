//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 27, 2005
 */
package org.globus.cog.karajan.parser;

public interface Lexer {
	boolean hasMoreTokens();
	boolean hasMoreChars();
	
	String nextToken();
	char nextChar();
	void skipChar();
	
	String peekToken();
	char peekChar();
	
	char peekNextChar(); 
	
	boolean isDigits();
	boolean isWhitespace();
	
	String currentLine();
	int getLineNumber();
	
	Object mark();
	void reset(Object mark);
	int getColumn();
    String region(Object b);
}
