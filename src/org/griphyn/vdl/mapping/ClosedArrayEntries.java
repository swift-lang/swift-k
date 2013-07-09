//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 5, 2013
 */
package org.griphyn.vdl.mapping;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.griphyn.vdl.karajan.Pair;

public class ClosedArrayEntries implements Iterable<List<?>> {
    private Map<Comparable<?>, DSHandle> array;

    public ClosedArrayEntries(Map<Comparable<?>, DSHandle> array) {
        this.array = array;
    }

    @Override
    public Iterator<List<?>> iterator() {
        final Iterator<Map.Entry<Comparable<?>, DSHandle>> it = array.entrySet().iterator();
        return new Iterator<List<?>>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public List<?> next() {
                Map.Entry<Comparable<?>, DSHandle> e = it.next();
                return new Pair(e.getKey(), e.getValue());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
