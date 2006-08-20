//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 8, 2005
 */
package org.globus.cog.karajan.util.serialization;

public class XMLUtils {
	public static final int LIMIT = 128; 
	public static final byte[] chars = new byte[LIMIT];
	public static final String[] escaped = new String[10];
	static {
		for(int i = 0; i < LIMIT; i++) {
			chars[i] = -1;
		}
		chars['<'] = 0; escaped[0] = "lt";
		chars['>'] = 1; escaped[1] = "gt";
		chars['&'] = 2; escaped[2] = "amp";
		chars['"'] = 3; escaped[3] = "quot";
		chars['\''] = 4; escaped[4] = "apos";
	}

	public static final String escape(String src) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < src.length(); i++) {
			char c = src.charAt(i);
			if (c < LIMIT) {
				int index = chars[c];
				if (index != -1) {
					sb.append('&');
					sb.append(escaped[index]);
					sb.append(';');
				}
				else {
					sb.append(c);
				}
			}
			else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

}
