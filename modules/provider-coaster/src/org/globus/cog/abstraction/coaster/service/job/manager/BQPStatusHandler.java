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
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.Block;
import org.globus.cog.abstraction.coaster.service.job.manager.Cpu;
import org.globus.cog.abstraction.coaster.service.job.manager.Job;
import org.globus.cog.abstraction.coaster.service.job.manager.Settings;
import org.globus.cog.abstraction.coaster.service.job.manager.SortedJobSet;
import org.globus.cog.abstraction.coaster.service.job.manager.SwingBQPMonitor;
import org.globus.cog.abstraction.coaster.service.job.manager.Time;
import org.globus.cog.abstraction.coaster.service.job.manager.TimeInterval;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.RequestHandler;

public class BQPStatusHandler extends RequestHandler {
    public static final Logger logger = Logger.getLogger(BQPStatusHandler.class);

    public static final String NAME = BQPStatusCommand.NAME;
    
    private int index;
    
    private static SwingBQPMonitor monitor;
    
    private static synchronized SwingBQPMonitor getMonitor() {
        if (monitor == null) {
            monitor = new SwingBQPMonitor();
        }
        return monitor;
    }

    public void requestComplete() throws ProtocolException {
        index = 0;
        sendReply("OK");
        try {
            Status status = new Status();
            String[] items;
            
            items = nextItem("settings");
            status.settings.set("lowOverallocation", items[0]);
            status.settings.set("highOverallocation", items[0]);
            status.settings.set("overallocationDecayFactor", items[0]);
                        
            items = nextItem("queuedsize");
            int qs = Integer.parseInt(items[0]);
            for (int i = 0; i < qs; i++) {
                items = nextItem("job");
                Job j = new Job();
                j.setMaxWallTime(timeInterval(items[0]));
                j.setStartTime(time(items[1]));
                j.setEndTime(time(items[2]));
                status.queued.add(j);
            }
            
            items = nextItem("jobssize");
            int jobssize = Integer.parseInt(items[0]);
            for (int i = 0; i < jobssize; i++) {
                items = nextItem("job");
                Job j = new Job();
                j.setMaxWallTime(timeInterval(items[0]));
                j.setStartTime(time(items[1]));
                j.setEndTime(time(items[2]));
                status.jobs.add(j);
            }
            
            items = nextItem("blockssize");
            int blockssize = Integer.parseInt(items[0]);
            for (int i = 0; i < blockssize; i++) {
                items = nextItem("block");
                Block b = new Block(items[0]);
                status.blocks.add(b);
                b.setWorkerCount(Integer.parseInt(items[1]));
                b.setCreationTime(time(items[2]));
                b.setStartTime(time(items[3]));
                b.setEndTime(time(items[4]));
                b.setDeadline(time(items[5]));
                b.setWalltime(timeInterval(items[6]));
                int cpus = Integer.parseInt(items[7]);
                
                for (int j = 0; j < cpus; j++) {
                    items = nextItem("cpu");
                    Cpu cpu = new Cpu();
                    cpu.setId(Integer.parseInt(items[0]));
                    boolean running = Boolean.valueOf(items[1]).booleanValue();
                    int donejobssize = Integer.parseInt(items[2]);
                    if (running) {
                        Job r = new Job();
                        items = nextItem("runningjob");
                        r.setMaxWallTime(timeInterval(items[0]));
                        r.setStartTime(time(items[1]));
                        cpu.setRunning(r);
                    }
                    
                    for (int k = 0; k < donejobssize; k++) {
                        items = nextItem("donejob");
                        Job d = new Job();
                        d.setMaxWallTime(timeInterval(items[0]));
                        d.setStartTime(time(items[1]));
                        d.setEndTime(time(items[2]));
                        cpu.addDoneJob(d);
                    }
                    b.getCpus().add(cpu);
                }
            }
            logger.info("Process BQP status update 1");
            getMonitor().update(status.settings, status.jobs, status.queued, status.blocks);
            logger.info("Process BQP status update 2");
        }
        catch (Exception e) {
            logger.warn("Failed to process data", e);
        }
    }

    private String nextString() {
        return getInDataAsString(index++);
    }
    
    private String[] nextItem(String type) {
        String s = nextString();
        if (!s.startsWith(type + ": ")) {
            throw new IllegalArgumentException("Invalid data line (" + s + "). Expected " + type + ".");
        }
        return s.substring(type.length() + 2).split("\\s+");
    }
    
    private static class Status {
        public Settings settings;
        public SortedJobSet queued;
        public List jobs, blocks;
        
        public Status() {
            settings = new Settings();
            queued = new SortedJobSet();
            jobs = new ArrayList();
            blocks = new ArrayList();
        }
    }
    
    private Time time(String s) {
        long i = Long.parseLong(s);
        if (i == -1) {
            return null;
        }
        else {
            return Time.fromMilliseconds(i);
        }
    }
    
    private TimeInterval timeInterval(String s) {
        long i = Long.parseLong(s);
        if (i == -1) {
            return null;
        }
        else {
            return TimeInterval.fromMilliseconds(i);
        }
    }
}
