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
 * Created on Apr 23, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;

public class Settings extends BaseSettings {
    public static final Logger logger = Logger.getLogger(Settings.class);

    /**
       Coasters will only consider settings listed here.
       workersPerNode is only included for its error message
     */
    public static final String[] NAMES = extend(BaseSettings.NAMES, 
        new String[] { "slots", 
                       "nodeGranularity", "allocationStepSize",
                       "maxNodes", "lowOverallocation",
                       "highOverallocation",
                       "overallocationDecayFactor",
                       "spread", "maxtime",
                       "remoteMonitorEnabled", 
                       "parallelism"});

    /**
     * The maximum number of blocks that can be active at one time
     */
    private int slots = 20;

    /**
     * How many nodes to allocate at once
     */
    private int nodeGranularity = 1;

    /**
     * When there is a need to allocate new blocks, how many should be used for
     * current jobs versus how many should be kept for future jobs (0.0 - 1.0)
     */
    private double allocationStepSize = 0.1;

    /**
     * How long (timewise) the request should be based on the job walltime.
     * lowOverallocation is the factor for 1s jobs
     * highOverallocation is the factor for +Inf jobs.
     * Things in-between are derived using x * ((os - oe) / x + oe.
     *
     * For example, with oe = 100, a bunch of jobs of walltime 1 will generate
     * blocks about 100 long.
     */
    private double lowOverallocation = 10, highOverallocation = 1;

    private double overallocationDecayFactor = 1.0 / 1000.0;

    /**
     * How to spread the size of blocks being allocated. 0 means no spread (all
     * blocks allocated in one iteration have the same size), and 1.0 is maximum
     * spread (first block will have minimal size, and the last block will be
     * twice the median).
     */
    private double spread = 0.9;

    /**
     * Maximum idle time of a block
     */
    private int exponentialSpread = 0;

    // this would cause bad things for jobsPerNode > 1024
    private int maxNodes = Integer.MAX_VALUE / 1024;

    private TimeInterval maxtime = TimeInterval.DAY.multiply(360);

    private boolean remoteMonitorEnabled;

	/**
	 * Adjusts the metric used for block sizes.
	 *
	 * Essentially when you pick a box, there is a choice of how you
	 * are going to pick the length vs. width of the box for the same
	 * volume.
	 *
	 * Though it's used a bit in reverse. A parallelism of 0 means
	 * that the size of a job will be its natural width * its height ^
	 * parallelism (the latter being 1). Since the height is always 1,
	 * then you can only fit 2 jobs in a volume 2 block, which has to
	 * have a width of 2.
	 *
	 * A parallelism of 1 means that the height is the actual
	 * walltime, which is realistic, but the parallelism will be
	 * whatever the block width happens to be. So basically it
	 * determines how much relative weight is given to the walltime
	 * vs. the number of CPUs needed.
	 */
    private double parallelism = 0.01;

    private TimeInterval maxWorkerIdleTime = TimeInterval.fromSeconds(120);

    /**
     * A pass-through setting for SGE, parallel environment
    */
    private String pe;

    public Settings() {
        super();
    }

    /**
       Formerly "slots": the maximum number of Coasters Blocks
     */
    public int getMaxBlocks() {
        return slots;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public int getNodeGranularity() {
        return nodeGranularity;
    }

    public void setNodeGranularity(int nodeGranularity) {
        this.nodeGranularity = nodeGranularity;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public double getAllocationStepSize() {
        return allocationStepSize;
    }

    public void setAllocationStepSize(double sz) {
        this.allocationStepSize = sz;
    }

    public double getLowOverallocation() {
        return lowOverallocation;
    }

    public void setLowOverallocation(double os) {
        this.lowOverallocation = os;
    }

    public double getHighOverallocation() {
        return highOverallocation;
    }

    public void setHighOverallocation(double oe) {
        this.highOverallocation = oe;
    }

    public double getOverallocationDecayFactor() {
        return overallocationDecayFactor;
    }

    public void setOverallocationDecayFactor(double overallocationDecayFactor) {
        this.overallocationDecayFactor = overallocationDecayFactor;
    }

    public double getSpread() {
        return spread;
    }

    public void setSpread(double spread) {
        this.spread = spread;
    }

    public int getExponentialSpread() {
        return exponentialSpread;
    }

    public void setExponentialSpread(int exponentialSpread) {
        this.exponentialSpread = exponentialSpread;
    }

    public TimeInterval getMaxtime() {
        return maxtime;
    }

    public void setMaxtime(String maxtime) {
        this.maxtime = TimeInterval.fromSeconds(WallTime.timeToSeconds(maxtime));
    }


    public boolean getRemoteMonitorEnabled() {
        return remoteMonitorEnabled;
    }

    public boolean isRemoteMonitorEnabled() {
        return remoteMonitorEnabled;
    }

    public TimeInterval getMaxWorkerIdleTime() {
        return maxWorkerIdleTime;
    }

    public void setMaxWorkerIdleTime(TimeInterval maxWorkerIdleTime) {
        this.maxWorkerIdleTime = maxWorkerIdleTime;
    }

    public void setRemoteMonitorEnabled(boolean monitor) {
        this.remoteMonitorEnabled = monitor;
    }

    public double getParallelism() {
        return parallelism;
    }

    public void setParallelism(double parallelism) {
        this.parallelism = parallelism;
    }
    
    @Override
    public String[] getNames() {
        return NAMES;
    }
}
