//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 22, 2011
 */
package org.griphyn.vdl.mapping;

import java.util.Comparator;

public class PathElementComparator implements Comparator<Comparable<?>> {

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Comparable<?> p1, Comparable<?> p2) {
        Comparable<Object> e1 = (Comparable<Object>) p1;
        Comparable<Object> e2 = (Comparable<Object>) p2;
        int d = 0;
        if (e1.getClass() != e2.getClass()) {
            d = e1.getClass().getName().compareTo(e2.getClass().getName());
        }
        if (d == 0) {
            d = e1.compareTo(e2);
        }
        return d;
    }
}
