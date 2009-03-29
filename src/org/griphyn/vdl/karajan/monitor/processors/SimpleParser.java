/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.processors;

public class SimpleParser {
	private int crt;
	private int tokStart;
	private int tokEnd;
	private String str;

	public SimpleParser(String str) {
		this.str = str;
	}

	public void skip(String tok) throws ParsingException {
		int index = str.indexOf(tok, crt);
		if (index == -1) {
			throw new ParsingException("Could not find \"" + tok + "\" in \"" + remaining()
					+ "\". String is \"" + str + "\".");
		}
		crt = index + tok.length();
	}

	public void markTo(String tok) throws ParsingException {
		int index = str.indexOf(tok, crt);
		if (index == -1) {
			throw new ParsingException("Could not find \"" + tok + "\" in \"" + remaining()
					+ "\". String is \"" + str + "\".");
		}
		tokEnd = index;
		crt = index + tok.length();
	}
	
	public void skipTo(String tok) throws ParsingException {
        int index = str.indexOf(tok, crt);
        if (index == -1) {
            throw new ParsingException("Could not find \"" + tok + "\" in \"" + remaining()
                    + "\". String is \"" + str + "\".");
        }
        crt = index + tok.length();
    }

	public boolean matchAndSkip(String str) {
		boolean b = this.str.regionMatches(crt, str, 0, str.length());
		if (b) {
			crt += str.length();
		}
		return b;
	}

	public String word() {
		skipWhitespace();
		beginToken();
		skipToWhitespace();
		endToken();
		skipWhitespace();
		return getToken();
	}

	public void skipWhitespace() {
		while (crt < str.length() && Character.isWhitespace(str.charAt(crt))) {
			crt++;
		}
	}

	public void skipToWhitespace() {
		while (crt < str.length() && !Character.isWhitespace(str.charAt(crt))) {
			crt++;
		}
	}

	public String remaining() {
		return str.substring(crt);
	}

	public void beginToken() {
		tokStart = crt;
	}

	public void endToken() {
		tokEnd = crt;
	}

	public String getToken() {
		return str.substring(tokStart, tokEnd);
	}

	public int getCrt() {
		return crt;
	}

	public String getStr() {
		return str;
	}

	public String toString() {
		return str.substring(crt);
	}
}
