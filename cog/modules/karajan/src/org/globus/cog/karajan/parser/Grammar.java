//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

import java.util.StringTokenizer;

import org.globus.cog.util.TextFileLoader;

public class Grammar {
	private Rules main;
	private AtomMapping mapping;

	public Grammar(String grammarFileName, AtomMapping mapping) {
		this.mapping = mapping;
		load(grammarFileName);
	}

	public final void load(String file) {
		String gr = new TextFileLoader().loadFromResource(file);
		StringTokenizer st = new GrammarTokenizer(gr);
		main = new Rules();
		main.read(new PeekableEnumeration(st), mapping);
		optimize();
	}
	
	public ParseTree parse(final Lexer lexer) throws ParsingException {
		return parseWithContext(lexer).tree;
	}

	public ParserContext parseWithContext(final Lexer lexer) throws ParsingException {
		ParserContext context = new ParserContext();
		context.tok = lexer;
		context.mapping = mapping;
		context.grammar = main;
		Stack stack = new Stack();
		if (!main.parse(context, stack)) {
			if (context.lastExpected == null) {
				throw new ParsingException("Unexpected input: " + lexer.currentLine());
			}
			else {
				throw new ParsingException("Expected " + context.lastExpected.errorForm() + "; got "
						+ lexer.currentLine());
			}
		}
		ParseTree cexp = new ParseTree(stack);
		context.tree = cexp;
		return context;
	}

	public ParseTree parse(String expr) throws ParsingException {
		return parse(new SimpleLexer(expr));
	}
	
	public ParserContext parseWithContext(String expr) throws ParsingException {
		return parseWithContext(new SimpleLexer(expr));
	}

	private void optimize() {
		main._optimize(main);
	}
	
	public static final class GrammarTokenizer extends StringTokenizer {
		public GrammarTokenizer(String str) {
			super(str, " \n\t");	
		}

		public String nextToken() {
			//TODO this is inconsistent with hasMoreTokens()
			//If the grammar ends with a comment, the tokenizer will break
			String tok = super.nextToken();
			while ("/*".equals(tok)) {
				while (!"*/".equals(tok)) {
					tok = super.nextToken();
				}
				tok = super.nextToken();
			}
			return tok;
		}
		
		
	}
}
