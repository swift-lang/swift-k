//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 21, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class Job implements Comparable {
    private int id;
    private Task task;
    private TimeInterval walltime;
    private Time starttime, endtime;
    private boolean done;

    private static int sid;
    
    public Job() {
    }

    public Job(Task t) {
        this.task = t;
        Object wt = ((JobSpecification) t.getSpecification()).getAttribute("maxwalltime");
        if (wt == null) {
            this.walltime = TimeInterval.fromSeconds(10 * 60);
        }
        else {
            this.walltime = TimeInterval.fromSeconds(new WallTime(wt.toString()).getSeconds());
        }
        synchronized (Job.class) {
            id = sid++;
        }
    }

    public Time getEndTime() {
        if (endtime == null) {
            if (starttime == null) {
                return null;
            }
            else {
                return starttime.add(walltime);
            }
        }
        else {
            return endtime;
        }
    }
    
    public void setEndTime(Time endtime) {
        this.endtime = endtime;
    }
    
    public Time getStartTime() {
        return starttime;
    }
    
    public void setStartTime(Time t) {
        this.starttime = t;
    }

    public boolean isDone() {
        return done;
    }

    public int compareTo(Object obj) {
        Job o = (Job) obj;
        TimeInterval diff = walltime.subtract(o.walltime);
        if (diff.getMilliseconds() == 0) {
            return id - o.id;
        }
        else {
            return (int) diff.getMilliseconds();
        }
    }
    
    public TimeInterval getMaxWallTime() {
        return walltime;
    }
    
    public void setMaxWallTime(TimeInterval walltime) {
        this.walltime = walltime;
    }

    public String toString() {
        return id + ":" + walltime;
    }

    public void start() {
        starttime = Time.now();
        endtime = Time.now().add(walltime);
    }

    public void fail(String message, Exception e) {
        task.setStatus(new StatusImpl(Status.FAILED, message, e));
    }
    
    public Task getTask() {
        return task;
    }
}