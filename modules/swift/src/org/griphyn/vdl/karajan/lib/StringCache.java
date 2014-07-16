//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 5, 2014
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Map;
import java.util.WeakHashMap;

public class StringCache {
    private static Map<String, String> cache = new WeakHashMap<String, String>();

    public synchronized static String get(String s) {
        String sc = cache.get(s);
        if (sc == null) {
        	cache.put(s, s);
        	return s;
        }
        else {
        	return sc;
        }
    }
}
