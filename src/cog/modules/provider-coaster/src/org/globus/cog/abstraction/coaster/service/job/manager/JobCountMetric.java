//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 10, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

public class JobCountMetric implements Metric {

    private Settings settings;
    
    public JobCountMetric(Settings settings) {
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
            return (int) tsum;
        }
        else {
            return (int) Math.ceil(minsize + (maxsize - minsize) * slot / (cslots - 1));
        }
    }

    public double getSize(Job j) {
        return j.mpiProcesses;
    }
    
    public double getSizeLeft(Job j) {
        return j.mpiProcesses;
    }

    public double desiredSize(Job j) {
        return j.mpiProcesses;
    }

    public double size(int w, int h) {
        return w;
    }

    public int width(int msz, int h) {
        return msz;
    }
}
