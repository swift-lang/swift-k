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
 * Created on Apr 21, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class Job implements Comparable<Job> {
    public static final Logger logger = Logger.getLogger(Job.class);

    private int id;
    private Task task;

    /**
       If not 1, number of MPI processes required (i.e., mpiexec -n)
       Set by JobSpecification attribute "mpi.processes"
     */
    private int count;

    /**
       If not 1, number of MPI processes required (i.e., mpiexec -n)
       Set by JobSpecification attribute "mpi.processes"
     */
    private int mpiPPN;

    private TimeInterval walltime;
    private Time starttime, endtime;
    private boolean done, canceled, running;

    private static int sid;
    
    private JobCancelator cancelator;
        
    public Job() {
    	synchronized (Job.class) {
    		id = sid++;
    	}
    }

    public Job(Task task) {
    	this();
        setTask(task);
    }

	public Job(Task task, int cpus) {
		this();
        setTask(task);
        this.count = cpus;
    }

    private void setTask(Task task) {
        this.task = task;
        if (task == null) {
            return;
        }

        JobSpecification spec = (JobSpecification) task.getSpecification();

        // Set walltime
        Object mwt = spec.getAttribute("maxwalltime");
        if (mwt == null) {
            this.walltime = TimeInterval.fromSeconds(10 * 60);
        }
        else {
            WallTime wt = new WallTime(mwt.toString());
            this.walltime = TimeInterval.fromSeconds(wt.getSeconds());
        }

        
        Object scount = spec.getAttribute("count");
        if (scount == null) {
            count = 1;
        }
        else {
            count = Integer.parseInt((String) scount);
            Object ppn = spec.getAttribute("mpi.ppn");
            if (ppn == null) {
            	mpiPPN = 1;
            }
            else {
            	mpiPPN = Integer.parseInt((String) ppn);
            }
            logger.info("MPI Job: id=" + id + " count=" + count + " mpi.ppn=" + mpiPPN);
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

    public int compareTo(Job other) {
        // Job other = (Job) obj;
        TimeInterval diff = walltime.subtract(other.walltime);
        if (diff.getMilliseconds() == 0) {
            return id - other.id;
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

    @Override
	public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Job(id:");
        sb.append(id);
        sb.append(" ");
        sb.append(walltime);
        if (count != 1) {
            sb.append(" [");
            sb.append(count);
            sb.append('/');
            sb.append(mpiPPN);
            sb.append("]");
        }
        sb.append(")");
        return sb.toString();
    }

    public void start() {
        starttime = Time.now();
        endtime = Time.now().add(walltime);
        if (logger.isDebugEnabled())
            logger.debug(this.toString() + " start: " +
                         starttime.getSeconds() + "-" +
                         endtime.getSeconds());
    }

    public void fail(String message, Exception e) {
        NotificationManager.getDefault().notificationReceived(task.getIdentity(), 
            new StatusImpl(Status.FAILED, message, e), null, null);
    }

    public Task getTask() {
        return task;
    }

    public int getCpus() {
        return count;
    }
    
    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getMpiPPN() {
        return mpiPPN;
    }

    public void setMpiPPN(int mpiPPN) {
        this.mpiPPN = mpiPPN;
    }

    public void cancel() {
        setCanceled(true);
        if (cancelator != null) {
            cancelator.cancel(this);
        }
    }

    public void setCancelator(JobCancelator cancelator) {
        this.cancelator = cancelator;
    }
}
