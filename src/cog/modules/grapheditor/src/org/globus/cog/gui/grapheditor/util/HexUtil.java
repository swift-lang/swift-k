
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 24, 2004
 */
package org.globus.cog.gui.grapheditor.util;

public class HexUtil {
	public static char[] hex32(int val) {
		char[] rt = new char[8];
		for (int i = 0; i < 8; i++) {
			rt[7-i] = hex4(val & 0x0000000f);
			val >>= 4;
		}
		return rt;
	}
	
	public static char[] hex24(int val) {
		char[] rt = new char[6];
		for (int i = 0; i < 6; i++) {
			rt[5-i] = hex4(val & 0x0000000f);
			val >>= 4;
		}
		return rt;
	}
	
	public static char hex4(int nibble) {
		if (nibble < 10) {
			return (char) ('0'+nibble);
		}
		return (char) ('a'+nibble-10);
	}
}
