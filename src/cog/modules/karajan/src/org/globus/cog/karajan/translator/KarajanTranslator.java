// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 14, 2005
 */
package org.globus.cog.karajan.translator;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.FileWriter;
import java.io.Reader;

import org.globus.cog.karajan.Configuration;
import org.globus.cog.karajan.parser.ParseTree;
import org.globus.cog.karajan.parser.Parser;

public class KarajanTranslator {
	private final Reader reader;
	private static Parser parser;
	private final String name;

	public KarajanTranslator(Reader r, String name) {
		this.reader = r;
		this.name = name;
	}
	
	private static synchronized Parser getParser() {
		if (parser == null) {
			parser = new Parser("karajan-language.gr", "karajan-language.map");
		}
		return parser;
	}

	public Reader translate() throws TranslationException {
		//TODO use streams in the parser
		StringBuffer sb = new StringBuffer();
		BufferedReader br;
		if (reader instanceof BufferedReader) {
			br = (BufferedReader) reader;
		}
		else {
			br = new BufferedReader(reader);
		}
		String line;
		try {
			do {
				line = br.readLine();
				if (line != null) {
					sb.append(line);
					sb.append('\n');
				}
			}
			while (line != null);
			ParseTree pt = getParser().parse(sb.toString());
			TranslationContext tc = new TranslationContext();
			pt.execute(tc);
			if (Configuration.getDefault().getFlag(Configuration.WRITE_INTERMEDIATE_SOURCE)) {
				FileWriter wr = new FileWriter(name.substring(0, name.lastIndexOf('.')) + ".kml");
				wr.write(tc.getWriter().toCharArray());
				wr.close();
			}
			return new CharArrayReader(tc.getWriter().toCharArray());
		}
		catch (Exception e) {
			throw new TranslationException(e);
		}
	}
}
