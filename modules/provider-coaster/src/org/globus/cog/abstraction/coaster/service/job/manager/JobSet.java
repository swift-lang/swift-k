/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
