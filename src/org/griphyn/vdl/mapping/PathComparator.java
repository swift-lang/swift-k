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

public class PathComparator implements Comparator<Path> {
    @SuppressWarnings("unchecked")
    public int compare(Path p1, Path p2) {
        for (int i = 0; i < Math.min(p1.size(), p2.size()); i++) {
            int d; 
            d = indexOrder(p1.isArrayIndex(i), p2.isArrayIndex(i));
            if (d != 0) {
                return d;
            }
            Comparable<Object> e1 = (Comparable<Object>) p1.getElement(i);
            Comparable<Object> e2 = (Comparable<Object>) p2.getElement(i);
            if (p1.isArrayIndex(i)) {
                if (e1.getClass() != e2.getClass()) {
                    d = e1.getClass().getName().compareTo(e2.getClass().getName());
                    
                }
            }
            if (d == 0) {
                d = e1.compareTo(e2);
            }
            if (d != 0) {
                return d;
            }
        }
        //the longer one wins
        return p1.size() - p2.size();
    }
    
    private int indexOrder(boolean i1, boolean i2) {
        //it doesn't matter much what the order between indices and non-indices is,
        //but it needs to be consistent
        if (i1) {
            if (!i2) {
                return -1;
            }
        }
        else {
            if (i2) {
                return 1;
            }
        }
        return 0;
    }
}