//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 21, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;


public class SortedJobSet {
    private SortedMap sm;
    int size;
    int jsize;
    int seq;

    public SortedJobSet() {
        sm = new TreeMap();
        size = 0;
    }

    public int size() {
        return size;
    }

    public synchronized void add(Job j) {
        LinkedList l = (LinkedList) sm.get(j.getMaxWallTime());
        if (l == null) {
            l = new LinkedList();
            sm.put(j.getMaxWallTime(), l);
        }
        l.add(j);
        jsize += j.getMaxWallTime().getSeconds();
        size++;
        seq++;
    }

    public synchronized Job removeOne(TimeInterval walltime) {
        // remove largest job with a walltime smaller than the specified value
        SortedMap sm2 = sm.headMap(walltime);
        if (sm2.isEmpty()) {
            return null;
        }
        else {
            TimeInterval key = (TimeInterval) sm2.lastKey();
            LinkedList jobs = (LinkedList) sm2.get(key);
            Job j = (Job) jobs.removeFirst();
            if (jobs.isEmpty()) {
                sm.remove(key);
            }
            if (j != null) {
                jsize -= j.getMaxWallTime().getSeconds();
                size--;
            }
            return j;
        }
    }

    public int getJSize() {
        return jsize;
    }

    public Iterator iterator() {
        return new Iterator() {
            private Iterator it1 = sm.values().iterator();
            private Iterator it2 = it1.hasNext() ? ((LinkedList) it1.next())
                .iterator() : null;

            public boolean hasNext() {
                return it2 != null && (it2.hasNext() || it1.hasNext());
            }

            public Object next() {
                if (it2.hasNext()) {
                    return it2.next();
                }
                else if (it1.hasNext()) {
                    it2 = ((LinkedList) it1.next()).iterator();
                    return it2.next();
                }
                else {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
            }
        };
    }

    public synchronized int getSeq() {
        return seq;
    };
}