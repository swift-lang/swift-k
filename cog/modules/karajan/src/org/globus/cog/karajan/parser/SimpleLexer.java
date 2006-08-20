// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

public final class SimpleLexer implements Lexer {
	private final char[] buf;
	private int begin, crt, line, lastLine;
	private int count, level;
	private String cachedToken;

	public SimpleLexer(final String str) {
		buf = str.toCharArray();
		crt = 0;
		count = 0;
		begin = 0;
		line = 1;
		scan();
	}

	private void scan() {
		count++;
		lastLine = line;
		crt = begin;
		if (crt == buf.length) {
			crt++;
		}
		if (crt > buf.length) {
			return;
		}
		level = getLevel(buf[crt]);
		begin = crt;
		while (true) {
			crt++;
			if (crt == buf.length) {
				break;
			}
			if (buf[crt] == '\n') {
				line++;
			}
			if (level == 3) {
				break;
			}
			int nlevel = getLevel(buf[crt]);
			if (nlevel != level) {
				if ((level == 1) && (nlevel == 2)) {
					continue;
				}
				break;
			}
		}
	}

	private int getLevel(final char c) {
		if (Character.isLetter(c)) {
			return 1;
		}
		if (Character.isWhitespace(c)) {
			return 0;
		}
		if (c >= '0' && c <= '9') {
			return 2;
		}
		return 3;
	}

	public String nextToken() {
		String n;
		if (cachedToken != null) {
			n = cachedToken;
			cachedToken = null;
		}
		else {
			n = String.valueOf(buf, begin, crt - begin);
		}
		begin = crt;
		scan();
		// System.err.println("TOKEN: " + n + " NEXT: " + String.valueOf(buf,
		// begin, crt - begin) + " ");
		return n;
	}

	public String peekToken() {
		if (begin >= buf.length) {
			return "EOF";
		}
		if (cachedToken == null) {
			cachedToken = String.valueOf(buf, begin, crt - begin);
		}
		return cachedToken;
	}

	public char peekChar() {
		return buf[begin];
	}

	/*
	 * Unfortunate hack to be able to deal with '=='
	 */
	public char peekNextChar() {
		if (buf.length > begin + 1) {
			return buf[begin+1];
		}
		return Character.MIN_VALUE;
	}

	public char nextChar() {
		cachedToken = null;
		char c = buf[begin++];
		if (begin == crt) {
			scan();
		}
		return c;
	}
	
	public void skipChar() {
		cachedToken = null;
		begin++;
		if (begin == crt) {
			scan();
		}
	}

	public boolean hasMoreChars() {
		return begin < buf.length;
	}

	public boolean hasMoreTokens() {
		return begin < buf.length;
	}

	public int getIndex() {
		return count;
	}

	public boolean isWhitespace() {
		return level == 0;
	}

	public boolean isDigits() {
		return level == 2;
	}

	public String currentLine() {
		int begin = this.begin;
		int end = crt;
		if (begin >= buf.length) {
			return "EOF";
		}
		while (begin > 0 && buf[begin] != '\n') {
			begin--;
		}
		while (end < buf.length && buf[end] != '\n') {
			end++;
		}
		return new String(buf, begin, end - begin);
	}

	public int getLineNumber() {
		return lastLine;
	}
	
	public Object mark() {
		return new Mark(begin, crt, line, lastLine, count, level, cachedToken);
	}

	public void reset(final Object mark) {
		Mark m = (Mark) mark;
		begin = m.begin;
		crt = m.crt;
		line = m.line;
		lastLine = m.lastLine;
		count = m.count;
		level = m.level;
		cachedToken = m.cachedToken;
	}
	
	private final class Mark {
		public final int begin, crt, line, lastLine;
		public final int count, level;
		public final String cachedToken;
		
		public Mark(int begin, int crt, int line, int lastLine, int count, int level, String cachedToken) {
			this.begin = begin;
			this.crt = crt;
			this.line = line;
			this.lastLine = lastLine;
			this.count = count;
			this.level = level;
			this.cachedToken = cachedToken;
		}
	}
}
