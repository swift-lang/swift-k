//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 9, 2015
 */
package org.globus.cog.util;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class StringCache {
    private static Map<String, WeakReference<String>> eden;
    private static Map<String, WeakReference<String>> survivor;
    private static final int MAX_EDEN_SIZE = 2048;
    private static final int MAX_SURVIVOR_SIZE = 2048;
        
    static {
        eden = Collections.synchronizedMap(new WeakHashMap<String, WeakReference<String>>());
        survivor = Collections.synchronizedMap(new WeakHashMap<String, WeakReference<String>>());
    }
    
    public static String intern(String str) {
        String cached = getCached(str, survivor);
        if (cached == null) {
            cached = removeCached(str, eden);
            if (cached == null) {
                add(str, eden, MAX_EDEN_SIZE);
                return str;
            }
            else {
                add(cached, survivor, MAX_SURVIVOR_SIZE);
                return cached;
            }
        }
        else {
            return cached;
        }
    }
    
    private static void add(String str, Map<String, WeakReference<String>> m, int maxSize) {
        if (m.size() > maxSize) {
            m.clear();
        }
        m.put(str, new WeakReference<String>(str));
    }

    private static String getCached(String str, Map<String, WeakReference<String>> m) {
        WeakReference<String> cached = m.get(str);
        if (cached == null) {
            return null;
        }
        return cached.get();
    }
    
    private static String removeCached(String str, Map<String, WeakReference<String>> m) {
        WeakReference<String> cached = m.remove(str);
        if (cached == null) {
            return str;
        }
        String cachedStr = cached.get();
        if (cachedStr == null) {
            m.put(str, new WeakReference<String>(str));
            return str;
        }
        return cachedStr;
    }

    public static int size() {
        return eden.size() + survivor.size();
    }
}
