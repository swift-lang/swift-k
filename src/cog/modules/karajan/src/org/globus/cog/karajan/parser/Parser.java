// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

public class Parser {
	private static final String GRAMMAR = "karajan-expression.gr";
	private static final String MAPPING = "karajan-expression.map";

	private AtomMapping mapping;
	private Grammar grammar;

	public Parser() {
		this(GRAMMAR, MAPPING);
	}

	public Parser(String grammar, String mapping) {
		this.mapping = new AtomMapping(mapping);
		this.grammar = new Grammar(grammar, this.mapping);
	}

	public ParseTree parse(String value) throws ParsingException {
		try {
			return grammar.parse(value);
		}
		catch (ParsingException e) {
			throw new ParsingException(e.getMessage(), e);
		}
	}
	
	public ParseTree parse(Lexer lexer) throws ParsingException {
		try {
			return grammar.parse(lexer);
		}
		catch (ParsingException e) {
			throw new ParsingException(e.getMessage(), e);
		}
	}
	
	public ParserContext parseWithContext(Lexer lexer) throws ParsingException {
		try {
			return grammar.parseWithContext(lexer);
		}
		catch (ParsingException e) {
			throw new ParsingException(e.getMessage(), e);
		}
	}
	
	public ParserContext parseWithContext(String exp) throws ParsingException {
		try {
			return grammar.parseWithContext(exp);
		}
		catch (ParsingException e) {
			throw new ParsingException(e.getMessage(), e);
		}
	}

	public static void main(String[] args) {
		Parser parser = new Parser();

		try {
			System.out.println(parser.parse("{variable}").treeString());
			System.out.println(parser.parse("{variable}and string").treeString());
			System.out.println(parser.parse("{variable}and {anotherVariable}").treeString());
			System.out.println(parser.parse("{variable}{anotherVariable2}").treeString());
			System.out.println(parser.parse("{variable-with#symbols}").treeString());
			System.out.println(parser.parse("").treeString());
			System.out.println(parser.parse("just a string").treeString());
			System.out.println(parser.parse("a string with # + - symbols").treeString());
		}
		catch (Exception e) {
			System.out.println("Caught exception:");
			e.printStackTrace();
		}
	}
}