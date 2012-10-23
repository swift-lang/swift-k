/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.toolkit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.log4j.Logger;
import org.globus.swift.parser.SwiftScriptLexer;
import org.globus.swift.parser.SwiftScriptParser;

/** Commandline tool to convert the textual form of SwiftScript into
	the XML form.

	Unix exit code is 0 on success; 1 on parse error; 2 on invocation error

*/

public class VDLt2VDLx {
	private static final Logger logger = Logger.getLogger(VDLt2VDLx.class);
	
	public static final String DEFAULT_TEMPLATE_FILE_NAME = "swiftscript.stg";

	public static void main(String[] args) throws Exception {
		try {
			if (args.length == 0) {
				compile(System.in, System.out);
			}
			else if (args.length == 1) {
				compile(args[0], System.in, System.out);
			}
			else {
				System.err.println("VDLt2VDLx: Too many parameters specified - expecting a single template filename");
				System.exit(2);
			}
		} catch(ParsingException e) {
			System.exit(1);
		}
		System.exit(0);
	}
	
	public static int compile(InputStream in, PrintStream out) throws ParsingException {
		return compile(DEFAULT_TEMPLATE_FILE_NAME, in, out);
	}

	public static int compile(String templateFileName, InputStream in, PrintStream out) 
        throws ParsingException {
		try {
			StringTemplateGroup templates;
			URL template = VDLt2VDLx.class.getClassLoader().getResource(templateFileName);
			if (template == null) {
				throw new RuntimeException("Invalid configuration. Template file (" + 
						templateFileName + ") not found on class path"); 
			}
			templates = new StringTemplateGroup(new InputStreamReader(template.openStream()));
			SwiftScriptLexer lexer = new SwiftScriptLexer(in);
			SwiftScriptParser parser = new SwiftScriptParser(lexer);
			parser.setTemplateGroup(templates);
			parser.setSwiftLexer(lexer);
			StringTemplate code = parser.program();
			out.println(code.toString());
		} catch(Exception e) {
			logger.error("Could not compile SwiftScript source: "+e);
			logger.debug("Full parser exception",e);
			throw new ParsingException("Could not compile SwiftScript source: " + e.getMessage(), e);
		}
		return 0;
	}

	static public class IncorrectInvocationException extends Exception {}
	static public class ParsingException extends Exception {

		public ParsingException() {
			super();
		}

		public ParsingException(String message, Throwable cause) {
			super(message, cause);
		}

		public ParsingException(String message) {
			super(message);	
		}

		public ParsingException(Throwable cause) {
			super(cause);
		}
	}

}

