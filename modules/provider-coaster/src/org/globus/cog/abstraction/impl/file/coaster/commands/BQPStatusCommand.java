//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 18, 2009
 */
package org.globus.cog.abstraction.impl.file.coaster.commands;

import java.util.Iterator;
import java.util.List;

import org.globus.cog.abstraction.coaster.service.job.manager.Block;
import org.globus.cog.abstraction.coaster.service.job.manager.Cpu;
import org.globus.cog.abstraction.coaster.service.job.manager.Job;
import org.globus.cog.abstraction.coaster.service.job.manager.Settings;
import org.globus.cog.abstraction.coaster.service.job.manager.SortedJobSet;
import org.globus.cog.abstraction.coaster.service.job.manager.Time;
import org.globus.cog.abstraction.coaster.service.job.manager.TimeInterval;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.commands.Command;

public class BQPStatusCommand extends Command {

    public static final String NAME = "BQPSTATUS";

    public BQPStatusCommand(Settings settings, List jobs, List blocks, SortedJobSet queued) {
        super(NAME);
        addOutData("settings: " + settings.getLowOverallocation() + " "
                + settings.getHighOverallocation() + " " + settings.getOverallocationDecayFactor());
        addOutData("queuedsize: " + queued.size());
        Iterator i = queued.iterator();
        while (i.hasNext()) {
            Job j = (Job) i.next();
            addOutData("job: " + getMS(j.getMaxWallTime()) + " " + getMS(j.getStartTime()) + " "
                    + getMS(j.getEndTime()));
        }
        addOutData("jobssize: " + jobs.size());
        i = jobs.iterator();
        while (i.hasNext()) {
            Job j = (Job) i.next();
            addOutData("job: " + getMS(j.getMaxWallTime()) + " " + getMS(j.getStartTime()) + " "
                    + getMS(j.getEndTime()));
        }
        addOutData("blockssize: " + blocks.size());
        i = blocks.iterator();
        while (i.hasNext()) {
            Block b = (Block) i.next();
            synchronized (b) {
                addOutData("block: " + b.getId() + " " + b.getWorkerCount() + " "
                        + getMS(b.getCreationTime()) + " " + getMS(b.getStartTime()) + " "
                        + getMS(b.getEndTime()) + " " + getMS(b.getDeadline()) + " "
                        + getMS(b.getWalltime()) + " " + b.getCpus().size());
                Iterator j = b.getCpus().iterator();
                while (j.hasNext()) {
                    Cpu c = (Cpu) j.next();
                    synchronized (c) {
                        Job running = c.getRunning();
                        addOutData("cpu: " + c.getId() + " " + (running != null) + " "
                                + c.getDoneJobs().size());
                        if (running != null) {
                            addOutData("runningjob: " + getMS(running.getMaxWallTime()) + " "
                                    + getMS(running.getStartTime()));
                        }
                        Iterator k = c.getDoneJobs().iterator();
                        while (k.hasNext()) {
                            Job dj = (Job) k.next();
                            addOutData("donejob: " + getMS(dj.getMaxWallTime()) + " "
                                    + getMS(dj.getStartTime()) + " " + getMS(dj.getEndTime()));
                        }
                    }
                }
            }
        }
    }

    private long getMS(Time t) {
        if (t == null) {
            return -1;
        }
        else {
            return t.getMilliseconds();
        }
    }

    private long getMS(TimeInterval t) {
        if (t == null) {
            return -1;
        }
        else {
            return t.getMilliseconds();
        }
    }    
}
