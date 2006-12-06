package org.griphyn.vdl.toolkit;

import java.io.InputStreamReader;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.griphyn.vdl.parser.VDLtLexer;
import org.griphyn.vdl.parser.VDLtParser;

public class VDLt2VDLx {
	public static void main(String[] args) throws Exception {
		String templateFileName = "XDTM.stg";
		if (args.length > 0) {
			templateFileName = args[0];
		}
		StringTemplateGroup templates;
		templates = new StringTemplateGroup(new InputStreamReader(
				VDLt2VDLx.class.getClassLoader().getResource(templateFileName).openStream()));
		VDLtLexer lexer = new VDLtLexer(System.in);
		VDLtParser parser = new VDLtParser(lexer);
		parser.setTemplateGroup(templates);
		StringTemplate code = parser.program();
		System.out.println(code.toString());
	}
}
