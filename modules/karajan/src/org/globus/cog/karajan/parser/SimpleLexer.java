// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

public final class SimpleLexer implements Lexer {
    public static final char EOF = '\u0000';
    private final char[] buf;
    private final int buflen;
    private int begin, crt, line, lastLine;
    private int count, level;
    private String cachedToken;

    public SimpleLexer(Reader src) throws IOException {
        buf = readSrc(src);
        buflen = buf.length;
        crt = 0;
        count = 0;
        begin = 0;
        line = 1;
        scan();
    }

    private char[] readSrc(Reader src) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(src);
		String line = br.readLine();
		while (line != null) {
			sb.append(line);
			sb.append('\n');
			line = br.readLine();
		}
		char[] b = new char[sb.length()];
		sb.getChars(0, sb.length(), b, 0);
		return b;
	}

	private void scan() {
        count++;
        lastLine = line;
        crt = begin;
        if (crt >= buflen) {
            crt = buflen + 1;
            return;
        }
        level = getLevel(buf[crt]);
        begin = crt;
        while (true) {
            crt++;
            if (crt == buflen) {
                break;
            }
            char c = buf[crt];
            if (c == '\n') {
                line++;
            }
            if (level == 3) {
                break;
            }
            int nlevel = getLevel(c);
            if (nlevel != level) {
                if ((level == 1) && (nlevel == 2)) {
                    continue;
                }
                break;
            }
        }
    }

    private static final int[] ccache = new int[256];

    static {
        Arrays.fill(ccache, -1);
        for (char c = 0; c < 256; c++) {
            ccache[c] = getLevel0(c);
        }
    }

    public static int getLevel(final char c) {
        if (c < 256) {
            return ccache[c];
        }
        else {
            return getLevel0(c);
        }
    }

    private static int getLevel0(final char c) {
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
        return n;
    }

    public String peekToken() {
        if (begin >= buflen) {
            return "EOF";
        }
        if (cachedToken == null) {
            cachedToken = String.valueOf(buf, begin, crt - begin);
        }
        return cachedToken;
    }

    public String toString() {
        return "SimpleLexer[nextToken = " + peekToken() + "]";
    }

    public char peekChar() {
        if (begin >= buflen) {
            return EOF;
        }
        return buf[begin];
    }

    public char peekNextChar() {
        if (buflen > begin + 1) {
            return buf[begin + 1];
        }
        return Character.MIN_VALUE;
    }

    public char nextChar() {
        cachedToken = null;
        if (begin >= buflen) {
            return EOF;
        }
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
        return begin < buflen;
    }

    public boolean hasMoreTokens() {
        return begin < buflen;
    }

    public int getIndex() {
        return count;
    }

    public boolean isWhitespace() {
        return level == 0 && begin < buflen;
    }

    public boolean isDigits() {
        return level == 2;
    }

    public String currentLine() {
        int begin = this.begin;
        int end = crt;
        int tabcount = 0;
        if (begin >= buflen) {
            return "EOF";
        }
        while (begin > 0 && buf[begin] != '\n') {
            begin--;
        }
        while (end < buf.length && buf[end] != '\n') {
            end++;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = begin; i < end; i++) {
            char c = buf[i];
            if (c == '\t') {
                sb.append("    ");
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public int getColumn() {
        int i = crt - 2;
        int tabcount = 0;
        while (i > 0 && buf[i] != '\n') {
            if (buf[i] == '\t') {
                tabcount++;
            }
            i--;
        }
        return crt - i - 1 + tabcount * 3;
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

    @Override
    public String region(Object b) {
        Mark m = (Mark) b;
        return new String(buf, m.begin, begin - m.begin);
    }

    private final class Mark {
        public final int begin, crt, line, lastLine;
        public final int count, level;
        public final String cachedToken;

        public Mark(int begin, int crt, int line, int lastLine, int count,
                int level, String cachedToken) {
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
