package org.griphyn.vdl.parser;

import java.io.*;

import org.antlr.stringtemplate.*;

import antlr.RecognitionException;
import antlr.TokenStreamException;

public class VDLExpression {
	private String templateFileName = "Karajan.stg";
	StringTemplateGroup templates = null;

	// provide a singleton
	private static VDLExpression singleton = null;

	public static VDLExpression instance(StringTemplateGroup templateGroup) throws IOException {
		if (singleton == null) {
			singleton = new VDLExpression(templateGroup);
		}
		return singleton;
	}

	/**
	 * constructor called only once by the instance
	 * 
	 * @throws IOException
	 */
	private VDLExpression(StringTemplateGroup templateGroup) throws IOException {
		if (templateGroup != null)
			this.templates = templateGroup;
		else {
			templates = new StringTemplateGroup(new InputStreamReader(
			VDLExpression.class.getClassLoader().getResourceAsStream(this.templateFileName)));
		}
	}

	/**
	 * returns null if unsuccessful
	 * 
	 * @param expression
	 * @return
	 * @throws RecognitionException
	 * @throws TokenStreamException
	 */
	public StringTemplate parse(String expression) throws RecognitionException, TokenStreamException {
		if (expression == null)
			return null;
		
		String expr = expression + ";";
		StringReader reader = new StringReader(expr);
		VDLtLexer lexer = new VDLtLexer(reader);
		VDLtParser parser = new VDLtParser(lexer);
		parser.setTemplateGroup(templates);
		StringTemplate exprST = null;
		try {
			exprST = parser.expression();
		}
		catch (RecognitionException e) {
			throw new RecognitionException(e + "\n in " + expression);
		}
		catch (TokenStreamException e) {
			throw new TokenStreamException(e + "\n in " + expression);
		}

		try {
			int c = reader.read();
			if(-1 != c) {
				throw new RuntimeException("didn't parse whole expression\n in " + expression + ". Next character is "+(char)c);
			}
		} catch(IOException e) {
			throw new RuntimeException("while checking that whole expression was parsed", e);
		}
		return exprST;
	}

	public static void main(String[] args) {
		try {
			VDLExpression ve = VDLExpression.instance(null);
			StringTemplate expr = ve.parse(args[0]);
			System.out.println(expr.toString());
		}
		catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}
}
