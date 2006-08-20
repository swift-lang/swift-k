
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.util;

/**
 * This class provides a string split function similar to jdk 1.4
 * String.split(). Be warned that the split() method in this class does not use
 * regular expressions.
 *  
 */
public class StringUtil {

	/**
	 * Splits a string into an array of tokens
	 * 
	 * @param string
	 *            The string to be split
	 * @param separator
	 *            The separator for the tokens
	 * @return an array with the tokens generated
	 */
	public static String[] split(String string, String separator) {

		int count = 0;
		for (int last = -1, next = 0;
			next != -1;
			next = string.indexOf(separator, last + 1), count++, last = next) {
		}
		String[] tokens = new String[count];
		int lastIndex = -1;
		int nextIndex = 0;
		int token = 0;
		do {
			nextIndex = string.indexOf(separator, lastIndex + 1);
			if (nextIndex == -1) {
				String last;
				if (lastIndex == string.length() - 1) {
					last = "";
				}
				else {
					last = string.substring(lastIndex + 1);
				}
				tokens[token++] = last;
			}
			else {
				tokens[token++] = string.substring(lastIndex + 1, nextIndex);
			}
			lastIndex = nextIndex;
		}
		while (lastIndex != -1);

		return tokens;
	}

	public static String wordWrap(String arg, int lead, int width) {
		StringBuffer tmp = new StringBuffer();
		String[] words = arg.split(" ");
		int crtwidth = 0;
		String sLead = StringUtil.repeat(" ", lead);
		tmp.append(sLead);
		for (int i = 0; i < words.length; i++) {
			if (crtwidth == 0) {
				if (words[i].length() > width) {
					String[] split = StringUtil.split(words[i], width);
					for (int j = 0; j < split.length - 1; j++) {
						tmp.append(split[j]);
						tmp.append("\n");
						tmp.append(sLead);
					}
					tmp.append(split[split.length - 1]);
					if (split[split.length - 1].length() < width) {
						tmp.append(" ");
						crtwidth = split[split.length - 1].length() + 1;
					}
					else {
						tmp.append("\n");
						tmp.append(sLead);
						crtwidth = 0;
					}
				}
				else {
					tmp.append(words[i]);
					tmp.append(" ");
					crtwidth += words[i].length() + 1;
				}
			}
			else {
				int len = words[i].length();
				if (crtwidth + len < width) {
					tmp.append(words[i]);
					tmp.append(" ");
					crtwidth += len + 1;
				}
				else if (crtwidth + len == width) {
					tmp.append(words[i]);
					tmp.append("\n");
					tmp.append(sLead);
					crtwidth = 0;
				}
				else {
					tmp.append("\n");
					tmp.append(sLead);
					tmp.append(words[i]);
					tmp.append(" ");
					crtwidth = len + 1;
				}
			}
		}
		return tmp.toString();
	}

	public static String repeat(String s, int times) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < times; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	public static String[] split(String s, int length) {
		if (length == 0) {
			return new String[0];
		}
		int len = s.length() / length + 1;
		String[] result = new String[len];
		for (int i = 0; i < len; i++) {
			result[i] = s.substring(i * length, Math.min((i + 1) * length, s.length()));
		}
		return result;
	}
}
