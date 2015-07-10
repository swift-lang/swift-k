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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.globus.cog.util.json.JSONEncoder;
import org.griphyn.vdl.karajan.monitor.monitors.http.BrowserDataBuilder.WorkerData;
import org.griphyn.vdl.karajan.monitor.processors.coasters.CoasterStatusItem.Block;
import org.griphyn.vdl.karajan.monitor.processors.coasters.CoasterStatusItem.Worker;
import org.griphyn.vdl.karajan.monitor.processors.coasters.WorkerProbeItem;

public class WorkerListBuilder {

    private BrowserDataBuilder db;
    private int page;
    private int pageSize;
    private String site;

    public WorkerListBuilder(BrowserDataBuilder db, String site, int page, int pageSize) {
        this.db = db;
        this.site = site;
        this.page = page;
        this.pageSize = pageSize;
    }

    public void getData(JSONEncoder e) {
        /*
         * worker, node running on, wall time, run time, #apps running
         * probes
         */
        Map<String, WorkerData> wd = db.getWorkerData();
        List<String> filtered = new ArrayList<String>();
        for (String wid : db.getWorkersByTime()) {
            WorkerData wdd = wd.get(wid);
            if (site == null || site.equals(wdd.site)) {
                filtered.add(wid);
            }
        }
        int start = (page - 1) * pageSize;
        int i = -1;
        e.beginMap();
        db.writePagingData(e, filtered.size(), page, pageSize);
        e.writeMapKey("data");
        e.beginArray();
        for (String wid : filtered) {
            i++;
            if (i < start) {
                continue;
            }
            if (i > start + pageSize) {
                break;
            }
            WorkerData wdd = wd.get(wid);
            
            e.beginArrayItem();
            e.beginMap();
            
            e.writeMapItem("id", wid);
            int index = wid.indexOf(':');
            String blkId = wid.substring(0, index);
            String wId = wid.substring(index + 1);
            
            Block blk = db.getBlock(blkId);
            Worker w = blk.getWorker(wId);
            
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
                e.writeMapItem("l", cpu.getLoad());
                e.endMap();
            }
            e.endArray();               
            e.endMap();
        }
        e.endArray();
        e.endMap();
    }

    private <T> T getLast(List<T> l) {
        if (l == null || l.isEmpty()) {
            return null;
        }
        return l.get(l.size() - 1);
    }

}
