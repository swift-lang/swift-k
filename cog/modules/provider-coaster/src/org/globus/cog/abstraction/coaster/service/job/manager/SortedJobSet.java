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

import org.apache.log4j.Logger;


public class SortedJobSet implements Iterable<Job> {
    public static final Logger logger = Logger.getLogger(SortedJobSet.class);
    
    private SortedMap<TimeInterval, LinkedList<Job>> sm;
    int size;
    double jsize;
    /** 
       Monotonically increasing job sequence number
     */
    int seq;
    private Metric metric;
    
    public SortedJobSet() {
        this(Metric.NULL_METRIC);
    }
    
    public SortedJobSet(Metric metric) {
        sm = new TreeMap<TimeInterval, LinkedList<Job>>();
        size = 0;
        this.metric = metric;
    }
    
    public SortedJobSet(SortedJobSet other) {
        metric = other.metric;
        synchronized(other) {
            sm = new TreeMap<TimeInterval, LinkedList<Job>>(other.sm);
            jsize = other.jsize;
            size = other.size;
        }
    }

    public int size() {
        return size;
    }

    public synchronized void add(Job j) {
        LinkedList<Job> l = sm.get(j.getMaxWallTime());
        if (l == null) {
            l = new LinkedList<Job>();
            sm.put(j.getMaxWallTime(), l);
        }
        l.add(j);
        jsize += metric.getSize(j);
        size++;
        seq++;
        if (logger.isDebugEnabled()) {
            logger.info("+" + j + "; " + sm);
        }
    }

    public synchronized Job removeOne(TimeInterval walltime) {
        // remove largest job with a walltime smaller than the specified value
        SortedMap<TimeInterval, LinkedList<Job>> sm2 = sm.headMap(walltime);
        if (sm2.isEmpty()) {
            return null;
        }
        else {
            TimeInterval key = sm2.lastKey();
            LinkedList<Job> jobs = sm2.get(key);
            Job j = jobs.removeFirst();
            if (jobs.isEmpty()) {
                sm.remove(key);
            }
            if (j != null) {
                jsize -= metric.getSize(j);
                size--;
            }
            if (size == 0) {
                jsize = 0;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("-" + j + "; " + sm);
            }
            return j;
        }
    }

    public double getJSize() {
        return jsize;
    }

    public Iterator<Job> iterator() {
        return new Iterator<Job>() {
            private Iterator<LinkedList<Job>> it1 = sm.values().iterator();
            private Iterator<Job> it2 = it1.hasNext() ? it1.next().iterator() : null;

            public boolean hasNext() {
                return it2 != null && (it2.hasNext() || it1.hasNext());
            }

            public Job next() {
                if (it2.hasNext()) {
                    return it2.next();
                }
                else if (it1.hasNext()) {
                    it2 = it1.next().iterator();
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
    }
    
    public String toString() {
        return sm.toString();
    }
}