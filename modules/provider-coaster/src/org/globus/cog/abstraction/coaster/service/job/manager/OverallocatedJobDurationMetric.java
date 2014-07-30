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
 * Created on Aug 10, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

public class OverallocatedJobDurationMetric implements Metric {

    private Settings settings;

    public OverallocatedJobDurationMetric(Settings settings) {
        this.settings = settings;
    }

    public double blockSize(int slot, int cslots, double tsum) {
        double medianSize = tsum / cslots;
        if (settings.getExponentialSpread() != 0) {
            int exp = (int) (Math.log(tsum / settings.getNodeGranularity()) / Math.log(settings.getExponentialSpread()));
            int crt = exp * slot / cslots;
            if (cslots == 0) {
                return 0;
            }
            else if (cslots == 1) {
                return settings.getNodeGranularity() * Math.pow(settings.getExponentialSpread(), exp);
            }
            else {
                return settings.getNodeGranularity() * Math.pow(settings.getExponentialSpread(), crt);
            }
        }
        else {
            double minsize = medianSize * (1 - settings.getSpread());
            double maxsize = medianSize * (1 + settings.getSpread());
            if (cslots == 0) {
                return 0;
            }
            else if (cslots == 1) {
                return tsum;
            }
            else {
                return Math.ceil(minsize + (maxsize - minsize) * slot / (cslots - 1));
            }
        }
    }

    public double getSize(Job j) {
        return pow(j.getMaxWallTime().getSeconds(), settings.getParallelism());
    }
    
    public double getSizeLeft(Job j) {
        TimeInterval timeLeft = j.getMaxWallTime();
        Time startTime = j.getStartTime();
        if (startTime != null) {
            timeLeft = timeLeft.subtract(Time.now().subtract(startTime));
        }
        return pow(timeLeft.getSeconds(), settings.getParallelism());
    }

    public double desiredSize(Job j) {
        return pow(BlockQueueProcessor.overallocatedSize(j, settings),
                   settings.getParallelism());
    }

    public double size(int w, int h) {
        return w * pow(h, settings.getParallelism());
    }

    public int width(int sz, int h) {
        return (int) (sz / pow(h, settings.getParallelism()));
    }

    private double pow(long v, double e) {
        return Math.pow(v, e);
    }

    private double pow(int v, double e) {
        return Math.pow(v, e);
    }
    
    public static void main(String[] args) {
        Settings s = new Settings();
        Metric m = new OverallocatedJobDurationMetric(s);
        s.setNodeGranularity(32);
        s.setExponentialSpread(2);
        System.out.println(m.blockSize(0, 4, 16384));
        System.out.println(m.blockSize(1, 4, 16384));
        System.out.println(m.blockSize(2, 4, 16384));
        System.out.println(m.blockSize(3, 4, 16384));
    }
}
