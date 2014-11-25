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
 * Created on Aug 7, 2013
 */
package org.griphyn.vdl.karajan.monitor.processors.coasters;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.processors.SimpleParser;

public class WorkerProbeProcessor extends AbstractRemoteLogProcessor {
    private static final Logger logger = Logger.getLogger(WorkerProbeProcessor.class);
    private CoasterStatusItem item;
    
    @Override
    public void initialize(SystemState state) {
        super.initialize(state);
    }

    @Override
    public String getMessageHeader() {
        return "PROBE";
    }

    @Override
    public void processMessage(SystemState state, SimpleParser p, Object details) {
        try {
            p.skip("type=");
            String type = p.word();
            p.skip("workerid=");
            String workerid = p.word();
            p.skip("time=");
            long time = (long) (1000 * Double.parseDouble(p.word()));
            
            StatefulItem item = null;
            
            if (type.equals("CPU")) {
                p.skip("load=");
                double load = Double.parseDouble(p.word());
                item = new WorkerProbeItem(workerid, new WorkerProbeItem.Cpu(time, load));
            }
            else if (type.equals("DF")) {
                p.skip("mount=");
                String mountPoint = p.immediateWord();
                p.skip("fs=");
                String fs = p.word();
                p.skip("used=");
                long usedBytes = 1024 * Long.parseLong(p.word());
                p.skip("avail=");
                long availBytes = 1024 * Long.parseLong(p.word());
                item = new WorkerProbeItem(workerid, new WorkerProbeItem.DiskUsage(time, mountPoint, fs, usedBytes, availBytes));
            }
            else if (type.equals("DL")) {
                p.skip("dev=");
                String device = p.word();
                p.skip("wtr=");
                int wtr = (int) Double.parseDouble(p.word());
                p.skip("rtr=");
                int rtr = (int) Double.parseDouble(p.word());
                p.skip("load=");
                double load = Double.parseDouble(p.word());
                item = new WorkerProbeItem(workerid, new WorkerProbeItem.IOLoad(time, device, wtr, rtr, load));
            }
            if (item != null) {
                state.itemUpdated(item);
            }
        }
        catch (Exception e) {
            logger.debug(new RuntimeException("Failed to parse log line: '" + p.getStr() + "'", e));
        }
    }
}
