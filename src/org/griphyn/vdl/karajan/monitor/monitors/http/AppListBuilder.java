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

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.globus.cog.util.json.JSONEncoder;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;

public class AppListBuilder {

    private String name;
    private int page;
    private int pageSize;
    private int stateFilter;
    private SortedMap<String, List<SortedSet<ApplicationItem>>> byName;
    private String hostFilter;
    private BrowserDataBuilder db;

    public AppListBuilder(BrowserDataBuilder db, String name, int page, int pageSize, int state, String host) {
        this.db = db;
        this.byName = db.getByName();
        this.name = name;
        if (name == null) {
            this.name = "";
        }
        this.page = page;
        this.pageSize = pageSize;
        this.stateFilter = state;
        this.hostFilter = host;
    }

    public void getData(JSONEncoder e) throws IOException {
        SortedSet<ApplicationItem> sorted = getInstances(name, stateFilter, hostFilter);
        
        if (pageSize == -1) {
            pageSize = sorted.size();
        }
        int start = (page - 1) * pageSize;
        int index = 0;
        e.beginMap();
        
        String title;
        if (stateFilter == -1) {
            if (name.isEmpty()) {
                title =  "All application invocations";
            }
            else {
                title = "Invocations of application \"" + name + "\"";
            }
        }
        else {
            if (name.isEmpty()) {
                title = ApplicationState.values()[stateFilter] + " application invocations";
            }
            else {
                title = ApplicationState.values()[stateFilter] + " invocations of application \"" + name + "\"";
            }
        }
        if (hostFilter != null) {
            title = title + " on site \"" + hostFilter + "\"";
        }
        e.writeMapItem("title", title);
        db.writePagingData(e, sorted.size(), page, pageSize);
        e.writeMapItem("name", name);
        e.writeMapItem("state", stateFilter);
        e.writeMapItem("host", hostFilter);
        for (ApplicationItem item : sorted) {
            if (index == start) {
                e.writeMapKey("data");
                e.beginArray();
            }
            if (index >= start) {
                ApplicationState state = item.getState();
                e.beginArrayItem();
                e.beginMap();
                e.writeMapItem("id", item.getID());
                e.writeMapItem("state", state.ordinal());
                e.writeMapItem("startTime", item.getStartTime());
                e.writeMapItem("host", item.getHost());
                if (item.getWorkerId() != null) {
                    e.writeMapItem("worker", item.getWorkerId());
                }
                e.writeMapItem("args", item.getArguments());
                if (state.isTerminal()) {
                    e.writeMapItem("runTime", item.getCurrentStateTime() - item.getStartTime());
                }
                else {
                    e.writeMapItem("runTime", 0L);
                }
                e.endMap();
                e.endArrayItem();
            }
            if (index > start + pageSize) {
                e.endArray();
                e.endMap();
                return;
            }
            index++;
        }
        if (sorted.size() > 0) {
            e.endArray();
        }
        e.endMap();
    }

    private SortedSet<ApplicationItem> getInstances(String name, int stateFilter, String hostFilter) {
        SortedSet<ApplicationItem> sorted = new TreeSet<ApplicationItem>(BrowserDataBuilder.APP_TIME_COMPARATOR);
        if (!name.isEmpty()) {
            List<SortedSet<ApplicationItem>> l = byName.get(name);
            if (l == null) {
                throw new RuntimeException("No such app name: '" + name + "'");
            }
            getInstances(sorted, l, stateFilter, hostFilter);
        }
        else {
            for (List<SortedSet<ApplicationItem>> l : byName.values()) {
                getInstances(sorted, l, stateFilter, hostFilter);
            }
        }
        return sorted;
    }
    
    private void getInstances(SortedSet<ApplicationItem> sorted, List<SortedSet<ApplicationItem>> l, int stateFilter, String hostFilter) {
        for (SortedSet<ApplicationItem> ss : l) {
            for (ApplicationItem app : ss) {
                boolean stateMatch = stateFilter == -1 || app.getState().ordinal() == stateFilter;
                boolean hostMatch = hostFilter == null || app.getHost().equals(hostFilter);
                if (stateMatch && hostMatch) {
                    sorted.add(app);
                }
            }
        }
    }
}
