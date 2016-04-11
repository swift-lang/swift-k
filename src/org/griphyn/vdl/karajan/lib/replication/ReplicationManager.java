/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on May 1, 2008
 */
package org.griphyn.vdl.karajan.lib.replication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.griphyn.vdl.util.SwiftConfig;

public class ReplicationManager {
    public static final Logger logger = Logger
            .getLogger(ReplicationManager.class);

    public static final int STATUS_NEEDS_REPLICATION = 100;

    public static final int INITIAL_QUEUE_TIME_ESTIMATE = 30; // seconds
    
    public static final int WALLTIME_DEADLINE_MULTIPLIER = 2;
    
    public static final int VERY_LARGE_WALLTIME = 360 * 24 * 60 * 60; //about one year

    private int n;
    private long s;
    private long s2;
    private Map<Task, Date> queued, running;
    private Map<Task, Integer> walltimes;
    private int minQueueTime, limit;
    private boolean enabled;
    private ReplicationGroups replicationGroups;
    private Scheduler scheduler;

    public ReplicationManager(Scheduler scheduler, SwiftConfig config) {
        this.replicationGroups = new ReplicationGroups(scheduler);
        this.scheduler = scheduler;
        queued = new HashMap<Task, Date>();
        running = new HashMap<Task, Date>();
        walltimes = new HashMap<Task, Integer>();
        try {
            minQueueTime = config.getReplicationMinQueueTime();
            enabled = config.isReplicationEnabled();
            limit = config.getReplicationLimit();
        }
        catch (Exception e) {
            logger.warn(
                    "Failed to get value of replication.min.queue.time property "
                            + "from Swift configuration. Using default (60s).",
                    e);
            minQueueTime = 60;
        }
        if (enabled) {
            Sweeper.getSweeper().register(this);
        }
    }

    public void register(String rg, Task task) throws CanceledReplicaException {
        if (enabled) {
            replicationGroups.add(rg, task);
            addWalltime(task);
        }
    }

    public void submitted(Task task, Date time) {
        if (enabled) {
            synchronized (this) {
                queued.put(task, time);
            }
        }
    }

    public void active(Task task, Date time) {
        if (enabled) {
            Date submitted;
            synchronized (this) {
                submitted = queued.remove(task);
                registerRunning(task, time);
                if (submitted != null) {
                    long delta = (time.getTime() - submitted.getTime()) / 1000;
                    n++;
                    s += delta;
                    s2 += delta * delta;
                }
            }
            replicationGroups.active(task);
        }
    }
    
    private void addWalltime(Task task) {
        JobSpecification spec = (JobSpecification) task.getSpecification();
        Object walltime = spec.getAttribute("maxwalltime");
        int seconds;
        if (walltime == null) {
            seconds = VERY_LARGE_WALLTIME;
        }
        else {
            seconds = WallTime.timeToSeconds(walltime.toString());
        }
        synchronized(this) {
            walltimes.put(task, seconds);
        }
    }

    protected void registerRunning(Task task, Date time) {
        synchronized (this) {
            int seconds = walltimes.remove(task);
            Date deadline = new Date(time.getTime() + WALLTIME_DEADLINE_MULTIPLIER * seconds * 1000);
            running.put(task, deadline);
        }
    }

    public synchronized int getN() {
        return n;
    }

    public synchronized double getMean() {
        if (n == 0) {
            return INITIAL_QUEUE_TIME_ESTIMATE;
        }
        else {
            return s / n;
        }
    }

    public synchronized double getStandardDeviation() {
        if (n == 0) {
            return 0;
        }
        else {
            return Math.sqrt((s2 - s * s / n) / n);
        }
    }

    public void checkTasks() {
        Map<Task, Date> m, r;
        synchronized (this) {
            m = new HashMap<Task, Date>(queued);
            r = new HashMap<Task, Date>(running);
        }
        for (Map.Entry<Task, Date> e : m.entrySet()) {
            Task t = e.getKey();
            Date d = e.getValue();
            if (shouldBeReplicated(t, d)) {
                replicationGroups.requestReplica(t);
            }
        }
        for (Map.Entry<Task, Date> e : r.entrySet()) {
            Task t = e.getKey();
            Date d = e.getValue();
            if (isOverDeadline(t, d)) {
                logger.info(t + ": deadline passed. Cancelling job.");
                cancelTask(t);
            }
        }
    }

    private boolean shouldBeReplicated(Task t, Date d) {
        if (t.getStatus().getStatusCode() == STATUS_NEEDS_REPLICATION) {
            // don't keep replicating the same job
            return false;
        }
        long inTheQueue = (System.currentTimeMillis() - d.getTime()) / 1000;
        if (inTheQueue > minQueueTime && inTheQueue > 3 * getMean()
                && replicationGroups.getRequestedCount(t) < limit) {
            return true;
        }
        else {
            return false;
        }
    }
    
    private boolean isOverDeadline(Task t, Date d) {
        if (System.currentTimeMillis() > d.getTime()) {
            return true;
        }
        else {
            return false;
        }
    }
    
    private void cancelTask(Task t) {
        scheduler.cancelTask(t, "Walltime exceeded");
        // prevent repeated cancelling in case the provider doesn't support cancel()
        synchronized (this) {
            running.remove(t);
        }
    }

    public void terminated(Task task) {
        synchronized (this) {
            running.remove(task);
        }
    }
}