//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 2, 2013
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StaticRefCount {
    public final String name;
    public final int count;
    
    public StaticRefCount(String name, int count) {
        this.name = name;
        this.count = count;
    }
    
    public static List<StaticRefCount> build(String refs) {
        if (refs == null) {
            return null;
        }
        List<StaticRefCount> l = new ArrayList<StaticRefCount>();
        String name = null;
        boolean flip = true;
        StringTokenizer st = new StringTokenizer(refs);
        while (st.hasMoreTokens()) {
            if (flip) {
                name = st.nextToken();
            }
            else {
                int count = Integer.parseInt(st.nextToken());
                l.add(new StaticRefCount(name.toLowerCase(), count));
            }
            flip = !flip;
        }
        return l;
    }
}