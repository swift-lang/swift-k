//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 7, 2013
 */
package org.griphyn.vdl.karajan.monitor.processors.coasters;

import java.util.Collection;

import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;

public class WorkerProbeItem implements StatefulItem {
    private String workerid;
    private final Data data;
    
    public WorkerProbeItem(String workerid, Data data) {
        this.workerid = workerid;
        this.data = data;
    }
    
    public Data getData() {
        return data;
    }



    public static class Data {
        private final long time;
        
        public Data(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }
    }

    public static class Cpu extends Data {
        private final double load;
        
        public Cpu(long time, double load) {
            super(time);
            this.load = load;
        }

        public double getLoad() {
            return load;
        }
    }
    
    public static class DiskUsage extends Data {
        private final String mountPoint, fs;
        private final long used, available;
        
        public DiskUsage(long time, String mountPoint, String fs, long used, long available) {
            super(time);
            this.mountPoint = mountPoint;
            this.fs = fs;
            this.used = used;
            this.available = available;
        }
        
        public String getMountPoint() {
            return mountPoint;
        }

        public String getFs() {
            return fs;
        }

        public long getUsed() {
            return used;
        }

        public long getAvailable() {
            return available;
        }
    }
    
    public static class IOLoad extends Data {
        private final String device;
        private final int writeThroughput, readThroughput;
        private final double load;
        
        public IOLoad(long time, String device, int writeThroughput, int readThroughput, double load) {
            super(time);
            this.device = device;
            this.writeThroughput = writeThroughput;
            this.readThroughput = readThroughput;
            this.load = load;
        }
        
        public String getDevice() {
            return device;
        }

        public long getWriteThroughput() {
            return writeThroughput;
        }

        public long getReadThroughput() {
            return readThroughput;
        }

        public double getLoad() {
            return load;
        }
    }

    @Override
    public StatefulItem getParent() {
        return null;
    }

    @Override
    public void setParent(StatefulItem parent) {
    }

    @Override
    public void addChild(StatefulItem child) {
    }

    @Override
    public void removeChild(StatefulItem child) {
    }

    @Override
    public Collection<StatefulItem> getChildren() {
        return null;
    }

    @Override
    public StatefulItemClass getItemClass() {
        return StatefulItemClass.MISC;
    }

    @Override
    public String getID() {
        return workerid;
    }

    @Override
    public void addListener(Listener l) {
    }
}
