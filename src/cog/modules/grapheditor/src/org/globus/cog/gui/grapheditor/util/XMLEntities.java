
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 14, 2003
 */
package org.globus.cog.gui.grapheditor.util;

import java.util.Hashtable;


public class XMLEntities{
	private static Hashtable map;
	
	static {
		map = new Hashtable();
		map.put(">", "&gt;");
		map.put("<", "&lt;");
		map.put("&", "&amp;");
	}
	
	public static String encodeEntity(char chr){
		if (map.containsKey(Character.toString(chr))){
			return (String) map.get(Character.toString(chr));
		}
		return Character.toString(chr);
	}
	
	public static String encodeString(String str){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++){
			sb.append(encodeEntity(str.charAt(i)));
		}
		return sb.toString();
	}
}
