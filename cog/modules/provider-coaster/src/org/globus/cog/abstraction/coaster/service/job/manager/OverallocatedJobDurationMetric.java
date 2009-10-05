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

    public double getSize(Job j) {
        return pow(j.getMaxWallTime().getSeconds(), settings.getParallelism());
    }

    public double desiredSize(Job j) {
        return pow(BlockQueueProcessor.overallocatedSize(j, settings), settings.getParallelism());
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
}
