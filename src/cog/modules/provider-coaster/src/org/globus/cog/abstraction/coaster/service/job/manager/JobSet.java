//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 18, 2011
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class JobSet implements Iterable<Job> {
    private Set<Job> jobs;
    private Metric metric;
    
    public JobSet(Metric metric) {
        this.metric = metric;
        this.jobs = new HashSet<Job>();
    }
    
    public void add(Job job) {
        synchronized(jobs) {
            jobs.add(job);
        }
    }
    
    public void remove(Job job) {
        synchronized(jobs) {
            jobs.remove(job);
        }
    }
    
    public double getSize() {
        double sum = 0;
        
        synchronized(jobs) {
            for (Job job : jobs) {
                sum += metric.getSize(job);
            }
        }
        
        return sum;
    }
    
    public double getSizeLeft() {
        double sum = 0;
        
        synchronized(jobs) {
            for (Job job : jobs) {
                sum += metric.getSizeLeft(job);
            }
        }
        
        return sum;
    }

    public Iterator<Job> iterator() {
        return jobs.iterator();
    }
    
    public int size() {
        return jobs.size();
    }
}
