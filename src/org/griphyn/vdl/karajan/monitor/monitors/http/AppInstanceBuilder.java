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
 * Created on Jun 29, 2014
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.util.json.JSONEncoder;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;
import org.griphyn.vdl.karajan.monitor.monitors.http.BrowserDataBuilder.AppEntry;
import org.griphyn.vdl.karajan.monitor.monitors.http.BrowserDataBuilder.TimedValue;

public class AppInstanceBuilder {

    private BrowserDataBuilder db;
    private String id;
    private Map<String, AppEntry> entries;
    private SystemState state;

    public AppInstanceBuilder(BrowserDataBuilder db, String id) {
        this.db = db;
        this.id = id;
        this.entries = db.getEntries();
        this.state = db.getState();
    }

    public void getData(JSONEncoder e) {
        AppEntry ae = entries.get(id);
        if (ae == null) {
            throw new IllegalArgumentException("Unknown application ID: " + id);
        }
        
        ApplicationItem app = ae.item;
        
        e.beginMap();
        e.writeMapItem("id", id);
        e.writeMapItem("name", app.getName());
        e.writeMapItem("args", app.getArguments());
        e.writeMapItem("host", app.getHost());
        e.writeMapItem("crtState", app.getState().ordinal());
        e.writeMapItem("totalTime", getTotalTime(ae.stateTimeline));
        e.writeMapItem("runTime", getRunTime(ae.stateTimeline));
        e.writeMapItem("timeline", db.getStateTimes(app));
                
        if (app.getWorkerId() != null) {
            e.writeMapItem("workerid", app.getWorkerId());
        }
        
        TaskItem et = null;
        
        if (ae.tasks != null) {
            for (TaskItem it : ae.tasks) {
                if (it.getType() == Task.JOB_SUBMISSION) {
                    et = it;
                }
            }
        }
        
        if (et != null) {
            Task t = et.getTask();
            JobSpecification spec = (JobSpecification) t.getSpecification();
            List<String> args = spec.getArgumentsAsList();
            extractJobInfo(e, args);
            e.writeMapItem("directory", spec.getDirectory());
        }
        e.endMap();
    }


    private int getTotalTime(List<TimedValue<ApplicationState>> tl) {
        if (tl == null || tl.isEmpty()) {
            return 0;
        }
        TimedValue<ApplicationState> lastState = tl.get(tl.size() - 1);
        return (int) (lastState.time - tl.get(0).time); 
    }
    
    private int getRunTime(List<TimedValue<ApplicationState>> tl) {
        if (tl == null || tl.isEmpty()) {
            return 0;
        }
        TimedValue<ApplicationState> lastState = tl.get(tl.size() - 1);
        long lastTime = lastState.time;
        if (!lastState.value.isTerminal()) {
            lastTime = state.getCurrentTime();
        }
        TimedValue<ApplicationState> lastStageIn = null;
        for (TimedValue<ApplicationState> p : tl) {
            if (p.value == ApplicationState.STAGE_IN) {
                lastStageIn = p;
            }
        }
        if (lastStageIn != null) {
            return (int) (lastTime - lastStageIn.time);
        }
        else {
            return 0;
        }
    }

    private void extractJobInfo(JSONEncoder e, List<String> args) {
        String key = null;
        List<String> l = new ArrayList<String>();
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (key != null) {
                    writeJobInfoItem(e, key, l);
                }
                key = arg;
                l.clear();
            }
            else {
                if ("-of".equals(key) || "-if".equals(key)) {
                    l.add(arg.replace("|", ", "));
                }
                else {
                    l.add(arg);
                }
            }
        }
    }

    private void writeJobInfoItem(JSONEncoder e, String key, List<String> l) {
        if (l.size() == 0) {
            return;
        }
        if (key.equals("-e")) {
            e.writeMapItem("executable", l.get(0));
        }
        else if (key.equals("-out")) {
            e.writeMapItem("stdout", l.get(0));
        }
        else if (key.equals("-err")) {
            e.writeMapItem("stderr", l.get(0));
        }
        else if (key.equals("-i")) {
            e.writeMapItem("stdin", l.get(0));
        }
        else if (key.equals("-if")) {
            e.writeMapItem("stagein", l);
        }
        else if (key.equals("-of")) {
            e.writeMapItem("stageout", l);
        }
        else if (key.equals("-a")) {
            e.writeMapItem("args", l);
        }
    }

}
