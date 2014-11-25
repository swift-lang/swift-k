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
	
	public void markMatchedTo(char m, char pair) throws ParsingException {
	    int level = 1;
	    for (int i = crt; i < str.length(); i++) {
	        char c = str.charAt(i);
	        if (c == m) {
	            level--;
	            if (level == 0) {
	                tokEnd = i;
	                return;
	            }
	        }
	        if (c == pair) {
	            level++;
	        }
	    }
        throw new ParsingException("Could not find \"" + m + "\" in \"" + remaining()
                    + "\". String is \"" + str + "\".");
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
	
	public String immediateWord() {
        beginToken();
        skipToWhitespace();
        endToken();
        skipWhitespace();
        return getToken().intern();
    }

	public String word() {
		skipWhitespace();
		return immediateWord();
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
