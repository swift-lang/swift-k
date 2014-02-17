//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 18, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.globus.cog.abstraction.coaster.service.job.manager.Block;
import org.globus.cog.abstraction.coaster.service.job.manager.Cpu;
import org.globus.cog.abstraction.coaster.service.job.manager.Job;
import org.globus.cog.abstraction.coaster.service.job.manager.Settings;
import org.globus.cog.abstraction.coaster.service.job.manager.SortedJobSet;
import org.globus.cog.abstraction.coaster.service.job.manager.Time;
import org.globus.cog.abstraction.coaster.service.job.manager.TimeInterval;
import org.globus.cog.coaster.commands.Command;

public class BQPStatusCommand extends Command {

    public static final String NAME = "BQPSTATUS";

    public BQPStatusCommand(Settings settings, List<Job> jobs, Collection<Block> blocks, SortedJobSet queued) {
        super(NAME);
        addOutData("settings: " + settings.getLowOverallocation() + " "
                + settings.getHighOverallocation() + " " + settings.getOverallocationDecayFactor());
        synchronized (queued) {
            addOutData("queuedsize: " + queued.size());
            for (Job j : queued) {
                addOutData("job: " + getMS(j.getMaxWallTime()) + " " + getMS(j.getStartTime())
                        + " " + getMS(j.getEndTime()));
            }
        }
        List<Job> mjobs = new ArrayList<Job>(jobs);
        addOutData("jobssize: " + mjobs.size());
        for (Job j : mjobs) {
            addOutData("job: " + getMS(j.getMaxWallTime()) + " " + getMS(j.getStartTime()) + " "
                    + getMS(j.getEndTime()));
        }
        List<Block> mblocks = new ArrayList<Block>(blocks);
        int bcnt = 0;
        for (Block b : mblocks) {
            if (!b.isDone()) {
                bcnt++;
            }
        }
        addOutData("blockssize: " + bcnt);
        for (Block b : mblocks) {
            if (b.isDone()) {
                continue;
            }
            synchronized (b) {
                addOutData("block: " + b.getId() + " " + b.getWorkerCount() + " "
                        + getMS(b.getCreationTime()) + " " + getMS(b.getStartTime()) + " "
                        + getMS(b.getEndTime()) + " " + getMS(b.getDeadline()) + " "
                        + getMS(b.getWalltime()) + " " + b.getCpus().size());
                List<Cpu> mcpus = new ArrayList<Cpu>(b.getCpus());
                for (Cpu c : mcpus) {
                    synchronized (c) {
                        Job running = c.getRunning();
                        addOutData("cpu: " + c.getId() + " " + (running != null) + " "
                                + c.getDoneJobs().size());
                        if (running != null) {
                            addOutData("runningjob: " + getMS(running.getMaxWallTime()) + " "
                                    + getMS(running.getStartTime()));
                        }
                        List<Job> mdoneJobs = new ArrayList<Job>(c.getDoneJobs());
                        for (Job dj : mdoneJobs) {
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
