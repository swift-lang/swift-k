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
 * Created on Jun 30, 2014
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.util.List;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.monitors.http.BrowserDataBuilder.WorkerData;
import org.griphyn.vdl.karajan.monitor.processors.coasters.CoasterStatusItem.Block;
import org.griphyn.vdl.karajan.monitor.processors.coasters.CoasterStatusItem.Worker;
import org.griphyn.vdl.karajan.monitor.processors.coasters.WorkerProbeItem;

public class WorkerInfoBuilder {

    private BrowserDataBuilder db;
    private String wid;

    public WorkerInfoBuilder(BrowserDataBuilder db, String wid) {
        this.db = db;
        this.wid = wid;
    }

    public void getData(JSONEncoder e) {
        /*
         * worker, node running on, wall time, run time, apps running
         * probes, all details
         */
        e.beginMap();
        Map<String, WorkerData> wd = db.getWorkerData();
            
        WorkerData wdd = wd.get(wid);
        
        int index = wid.indexOf(':');
        String blkId = wid.substring(0, index);
        String wId = wid.substring(index + 1);
        
        Block blk = db.getBlock(blkId);
        Worker w = blk.getWorker(wId);
        
        e.writeMapItem("id", wid);
        e.writeMapItem("node", w.node);
        e.writeMapItem("cores", w.cores);
        e.writeMapItem("startTime", blk.startTime);
        e.writeMapItem("walltime", blk.walltime);
        e.writeMapItem("activeApps", wdd.activeApps);
        e.writeMapItem("failedApps", wdd.failedApps);
        e.writeMapItem("completedApps", wdd.completedApps);
     
        e.writeMapKey("cpuLoad");
        e.beginArray();
        for (WorkerProbeItem.Cpu cpu : wdd.cpuLoad) {
            e.beginArrayItem();
            e.beginMap();
            e.writeMapItem("t", cpu.getTime());
            e.writeMapItem("load", cpu.getLoad());
            e.endMap();
        }
        e.endArray();
        
        e.writeMapKey("diskUsage");
        e.beginArray();
        int ix = 0;
        for (Map.Entry<String, List<WorkerProbeItem.DiskUsage>> e1 : wdd.diskUsage.entrySet()) {
            e.beginArrayItem();
            e.beginMap();
            e.writeMapItem("index", ix++);
            e.writeMapItem("mount", e1.getKey());
            e.writeMapKey("data");
            e.beginArray();
            for (WorkerProbeItem.DiskUsage du : e1.getValue()) {
                e.beginArrayItem();
                e.beginMap();
                e.writeMapItem("t", du.getTime());
                e.writeMapItem("avail", du.getAvailable());
                e.writeMapItem("used", du.getUsed());
                e.endMap();
            }
            e.endArray();
            e.endMap();
        }
        e.endArray();
        
        e.writeMapKey("ioLoad");
        e.beginArray();
        ix = 0;
        for (Map.Entry<String, List<WorkerProbeItem.IOLoad>> e1 : wdd.ioLoad.entrySet()) {
            e.beginArrayItem();
            e.beginMap();
            e.writeMapItem("index", ix++);
            e.writeMapItem("device", e1.getKey());
            e.writeMapKey("data");
            e.beginArray();
            for (WorkerProbeItem.IOLoad du : e1.getValue()) {
                e.beginArrayItem();
                e.beginMap();
                e.writeMapItem("t", du.getTime());
                e.writeMapItem("rt", du.getReadThroughput());
                e.writeMapItem("wt", du.getWriteThroughput());
                e.writeMapItem("load", du.getLoad());
                e.endMap();
            }
            e.endArray();
            e.endMap();
        }
        e.endArray();
                        
        e.endMap();
    }
}
