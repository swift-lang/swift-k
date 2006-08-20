//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 5, 2005
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.util.Hashtable;
import java.util.Map;

public class Aliases {
	private static Map aliases;

	public synchronized static void add(String from, String to) {
		if (aliases == null) {
				aliases = new Hashtable();
		}
		aliases.put(from, to);
	}
	
	public static boolean hasAlias(String name) {
		if (aliases == null) {
			return false;
		}
		return aliases.containsKey(name);
	}
	
	public static String getAlias(String name) {
		if (aliases == null) {
			return null;
		}
		return (String) aliases.get(name);
	}
}
