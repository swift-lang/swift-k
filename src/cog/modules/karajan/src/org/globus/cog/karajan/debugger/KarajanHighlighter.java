// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 11, 2005
 */
package org.globus.cog.karajan.debugger;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class KarajanHighlighter {
	public static final String ELEMENT = "element";
	public static final String ATTRNAME = "attrname";
	public static final String ATTRVALUE = "attrvalue";
	public static final String DEFAULT = "default";
	public static final String VAR = "var";
	public static final String COMMENT = "comment";

	private static Map styles;
	
	private static void setDefaults(SimpleAttributeSet style) {
		StyleConstants.setFontFamily(style, "monospaced");
		StyleConstants.setFontSize(style, 10);
		StyleConstants.setBackground(style, Color.white);
		StyleConstants.setItalic(style, false);
	}

	static {
		styles = new Hashtable();
		SimpleAttributeSet style;
		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.black);
		StyleConstants.setBold(style, false);
		styles.put(DEFAULT, style);

		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, new Color(10, 10, 128));
		StyleConstants.setBold(style, true);
		styles.put(ELEMENT, style);

		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.black);
		StyleConstants.setBold(style, false);
		styles.put(ATTRNAME, style);

		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, new Color(30, 120, 10));
		StyleConstants.setBold(style, false);
		styles.put(ATTRVALUE, style);

		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.black);
		StyleConstants.setBold(style, true);
		styles.put(VAR, style);

		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, new Color(96, 96, 96));
		StyleConstants.setBold(style, false);
		StyleConstants.setItalic(style, true);
		styles.put(COMMENT, style);
	}

	private int index;
	private char[] text;

	private String lastStyle;

	private static final int SNORMAL = 0;
	private static final int STAG = 1;
	private static final int SATTRNAME = 2;
	private static final int SATTRVAL = 3;
	private static final int SATTRVALREST = 6;
	private static final int SVAR = 4;
	private static final int SCOMMENT = 5;

	private int state = SNORMAL;

	private int line;
	
	public void setCurrentText(String text) {
		this.text = text.toCharArray();
		index = 0;
		lastStyle = DEFAULT;
		this.line = 0;
	}

	public boolean hasMoreTokens() {
		return more();
	}

	private StringBuffer sb;

	public String nextToken() {
		sb = new StringBuffer();

		switch (state) {
			case SNORMAL:
				appendUntil(sb, '<');
				if (index < text.length) {
					if (text[index] == '/') {
						sb.append(advance());
						state = STAG;
					}
					else if (text[index] == '!') {
						sb.append(advance());
						sb.append(advance());
						sb.append(advance());
						state = SCOMMENT;
					}
					else {
						state = STAG;
					}
				}
				lastStyle = DEFAULT;
				break;
			case STAG:
				char c = appendUntilWhitespace(sb, '/', '>');
				lastStyle = ELEMENT;
				if (c == '/' || c == '>') {
					state = SNORMAL;
				}
				else {
					state = SATTRNAME;
				}
				break;
			case SATTRNAME:
				c = appendUntil(sb, '=', '/', '>');
				lastStyle = ATTRNAME;
				if (c == '=') {
					sb.append(advance());
					state = SATTRVAL;
				}
				else {
					state = SNORMAL;
				}
				break;
			case SATTRVAL:
				appendUntil(sb, '"');
			case SATTRVALREST:
				c = appendUntil(sb, '"', '{');
				lastStyle = ATTRVALUE;
				if (c == '"') {
					state = SATTRNAME;
					sb.append('"');
					index++;
				}
				else {
					state = SVAR;
				}
				break;
			case SVAR:
				appendUntil(sb, '}');
				state = SATTRVALREST;
				lastStyle = VAR;
				break;
			case SCOMMENT:
				boolean end = false;
				while (!end) {
					appendUntilNotIncl(sb, '-');
					if (index + 2 < text.length) {
						if (text[index + 1] == '-' && text[index + 2] == '>') {
							lastStyle = COMMENT;
							end = true;
						}
					}
				}
				state = SNORMAL;
				break;
		}

		return sb.toString();
	}

	private void appendUntil(StringBuffer sb, char c) {
		while (more() && current() != c) {
			sb.append(advance());
		}
		if (more()) {
			sb.append(advance());
		}
	}

	private void appendUntilNotIncl(StringBuffer sb, char c) {
		while (more() && current() != c) {
			sb.append(advance());
		}
	}

	private char appendUntil(StringBuffer sb, char c1, char c2) {
		while (more() && (current() != c1) && (current() != c2)) {
			sb.append(advance());
		}
		if (more()) {
			return current();
		}
		else {
			return 0;
		}
	}

	private char appendUntil(StringBuffer sb, char c1, char c2, char c3) {
		while (more() && (current() != c1) && (current() != c2)
				&& (current() != c3)) {
			sb.append(advance());
		}
		if (more()) {
			return current();
		}
		else {
			return 0;
		}
	}

	private void appendUntilWhitespace(StringBuffer sb) {
		while (more() && !Character.isWhitespace(current())) {
			sb.append(advance());
		}
		while (more() && Character.isWhitespace(current())) {
			sb.append(advance());
		}
	}

	private char appendUntilWhitespace(StringBuffer sb, char c1, char c2) {
		while (more() && !Character.isWhitespace(current()) && (current() != c1)
				&& (current() != c2)) {
			sb.append(advance());
		}
		while (more() && Character.isWhitespace(current())) {
			sb.append(advance());
		}
		return text[index - 1];
	}

	public AttributeSet getStyle() {
		return (AttributeSet) styles.get(lastStyle);
	}

	public String getStyleName() {
		return lastStyle;
	}
	
	public int getLine() {
		return line;
	}
	
	private char advance() {
		char c = text[index++];
		if (c == '\n') {
			line++;
		}
		return c;
	}
	
	private char current() {
		return text[index];
	}
	
	private boolean more() {
		return index < text.length;
	}
}
