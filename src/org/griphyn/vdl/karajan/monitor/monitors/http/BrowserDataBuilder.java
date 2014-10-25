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
 * Created on Jul 21, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;
import org.griphyn.vdl.karajan.monitor.processors.coasters.CoasterStatusItem.Block;
import org.griphyn.vdl.karajan.monitor.processors.coasters.CoasterStatusItem;
import org.griphyn.vdl.karajan.monitor.processors.coasters.WorkerProbeItem;

public class BrowserDataBuilder extends StateDataBuilder implements SystemStateListener {
    
    public static final Comparator<ApplicationItem> APP_TIME_COMPARATOR = new Comparator<ApplicationItem>() {
        @Override
        public int compare(ApplicationItem o1, ApplicationItem o2) {
            return (int) (o1.getStartTime() - o2.getStartTime());
        }
    };
    
    public static class TimedValue<T> {
        public long time;
        public T value;
        
        public TimedValue(long time, T value) {
            this.time = time;
            this.value = value;
        }
    }
    
    public static class AppEntry {
        public ApplicationItem item;
        public ApplicationState oldState;
        public List<TaskItem> tasks;
        public List<TimedValue<ApplicationState>> stateTimeline;
    }
    
    public static class WorkerData {
        public String site;
        public List<WorkerProbeItem.Cpu> cpuLoad;
        public Map<String, List<WorkerProbeItem.DiskUsage>> diskUsage;
        public Map<String, List<WorkerProbeItem.IOLoad>> ioLoad;
        public int activeApps;
        public int failedApps;
        public int completedApps;
        
        public WorkerData() {
            cpuLoad = new ArrayList<WorkerProbeItem.Cpu>();
            diskUsage = new TreeMap<String, List<WorkerProbeItem.DiskUsage>>();
            ioLoad = new TreeMap<String, List<WorkerProbeItem.IOLoad>>();
        }
    }
    
    private final SystemState state;
    private int maxCount;
    private JSONEncoder e;
    
    private SortedMap<String, List<SortedSet<ApplicationItem>>> byName;
    private Map<String, AppEntry> entries;
    private List<ApplicationItem> byTime;
    
    private SortedMap<String, WorkerData> workerData;
    private List<String> workersByTime;

    public BrowserDataBuilder(SystemState state) {
        this.state = state;
        byName = new TreeMap<String, List<SortedSet<ApplicationItem>>>();
        byTime = new ArrayList<ApplicationItem>();
        entries = new HashMap<String, AppEntry>();
        workerData = new TreeMap<String, WorkerData>();
        workersByTime = new ArrayList<String>();
        state.addListener(this);
    }

    @Override
    public void itemUpdated(UpdateType updateType, StatefulItem item) {
        if (item.getItemClass() == StatefulItemClass.APPLICATION) {
            appUpdated(updateType, (ApplicationItem) item);
        }
        else if (item.getItemClass() == StatefulItemClass.TASK) {
            taskUpdated(updateType, (TaskItem) item);
        }
        else if (item instanceof WorkerProbeItem) {
            addWorkerProbeData((WorkerProbeItem) item);
        }
    }
    
    private WorkerData getWorkerData(String id) {
        WorkerData wd = workerData.get(id);
        if (wd == null) {
            wd = new WorkerData();
            workerData.put(id, wd);
            workersByTime.add(id);
        }
        return wd;
    }

    private void addWorkerProbeData(WorkerProbeItem item) {
        WorkerData wd = getWorkerData(item.getID());
        WorkerProbeItem.Data data = item.getData();
        if (data instanceof WorkerProbeItem.Cpu) {
            wd.cpuLoad.add((WorkerProbeItem.Cpu) data);
        }
        else if (data instanceof WorkerProbeItem.DiskUsage) {
            WorkerProbeItem.DiskUsage du = (WorkerProbeItem.DiskUsage) data;
            List<WorkerProbeItem.DiskUsage> l = wd.diskUsage.get(du.getMountPoint());
            if (l == null) {
                l = new ArrayList<WorkerProbeItem.DiskUsage>();
                wd.diskUsage.put(du.getMountPoint(), l);
            }
            l.add(du);
        }
        else if (data instanceof WorkerProbeItem.IOLoad) {
            WorkerProbeItem.IOLoad du = (WorkerProbeItem.IOLoad) data;
            List<WorkerProbeItem.IOLoad> l = wd.ioLoad.get(du.getDevice());
            if (l == null) {
                l = new ArrayList<WorkerProbeItem.IOLoad>();
                wd.ioLoad.put(du.getDevice(), l);
            }
            l.add(du);
        }
    }

    private void taskUpdated(UpdateType updateType, TaskItem task) {
        ApplicationItem app = (ApplicationItem) task.getParent();
        if (app == null) {
            return;
        }
        AppEntry e = entries.get(app.getID());
        if (e == null) {
            return;
        }
        if (e.tasks == null) {
            e.tasks = Collections.singletonList(task);
        }
        else if (e.tasks.size() == 1) {
            if (!e.tasks.contains(task)) {
                List<TaskItem> l = new LinkedList<TaskItem>();
                l.add(e.tasks.get(0));
                l.add(task);
                e.tasks = l;
            }
        }
        else {
            if (!e.tasks.contains(task)) {
                e.tasks.add(task);
            }
        }
    }

    private void appUpdated(UpdateType updateType, ApplicationItem app) {
        if (app.getName() == null) {
            return;
        }
        if (entries.containsKey(app.getID())) {
            updateApp(app);
        }
        else {
            addApp(app);
        }
    }

    private void updateApp(ApplicationItem item) {
        AppEntry e = entries.get(item.getID());
        ApplicationState old = e.oldState;
        ApplicationState state = item.getState();
        e.oldState = state;
        String name = item.getName();
        List<SortedSet<ApplicationItem>> l = getNamed(name);
        if (old != null) {
            l.get(old.ordinal()).remove(item);
        }
        l.get(state.ordinal()).add(item);
        List<TimedValue<ApplicationState>> timeline = getTimeline(e);
        
        timeline.add(new TimedValue<ApplicationState>(this.state.getCurrentTime(), state));
        
        if (item.getWorkerId() != null) {
            // initialize worker data
            WorkerData wd = getWorkerData(item.getWorkerId());
            switch (state) {
                case ACTIVE:
                    wd.activeApps++;
                    break;
                case FAILED:
                    wd.failedApps++;
                    wd.activeApps--;
                    break;
                case FINISHED_SUCCESSFULLY:
                    wd.activeApps--;
                    wd.completedApps++;
                    break;
            }
            wd.site = item.getHost();
        }
    }

    private List<TimedValue<ApplicationState>> getTimeline(AppEntry e) {
        List<TimedValue<ApplicationState>> tl = e.stateTimeline;
        if (tl == null) {
            tl = new ArrayList<TimedValue<ApplicationState>>();
            e.stateTimeline = tl;
        }
        return tl;
    }
    
    List<TimedValue<ApplicationState>> getTimeline(ApplicationItem item) {
        AppEntry e = entries.get(item.getID());
        if (e == null) {
            throw new IllegalArgumentException("Unknown app id: " + item.getID());
        }
        return getTimeline(e);
    }

    private List<SortedSet<ApplicationItem>> getNamed(String name) {
        List<SortedSet<ApplicationItem>> l = byName.get(name);
        if (l == null) {
            l = new ArrayList<SortedSet<ApplicationItem>>();
            for (ApplicationState s : ApplicationState.values()) {
                l.add(new TreeSet<ApplicationItem>(APP_TIME_COMPARATOR));
            }
            byName.put(name, l);
        }
        return l;
    }

    private void addApp(ApplicationItem item) {
        byTime.add(item);
        String name = item.getName();
        if (name == null) {
            return;
        }
        ApplicationState state = item.getState();
        AppEntry e = new AppEntry();
        entries.put(item.getID(), e);
        e.item = item;
        e.oldState = state;
        List<SortedSet<ApplicationItem>> l = getNamed(name);
        l.get(state.ordinal()).add(item);
    }

    @Override
    public ByteBuffer getData(Map<String, String> params) {
        e = new JSONEncoder();
        String type = getParam(params, "type", "apps");
        
        if (type.equals("apps")) {
            new AppsSummaryBuilder(this).getData(e);
        }
        else if (type.equals("applist")) {
            new AppListBuilder(this, getParam(params, "name", null), 
                Integer.parseInt(getParam(params, "page", "1")),
                Integer.parseInt(getParam(params, "pageSize", "-1")),
                Integer.parseInt(getParam(params, "state", "-1")), 
                getParam(params, "host", null)).getData(e);
        }
        else if (type.equals("appdetail")) {
            new AppDetailBuilder(this, getParam(params, "name")).getData(e);
        }
        else if (type.equals("appinstance")) {
            new AppInstanceBuilder(this, getParam(params, "id")).getData(e);
        }
        else if (type.equals("sites")) {
            new SiteInfoBuilder(this).getData(e);
        }
        else if (type.equals("workerlist")) {
            new WorkerListBuilder(this, getParam(params, "site", null), 
                Integer.parseInt(getParam(params, "page", "1")),
                Integer.parseInt(getParam(params, "pageSize", "20"))).getData(e);
        }
        else if (type.equals("worker")) {
            new WorkerInfoBuilder(this, getParam(params, "id")).getData(e);
        }
        else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        return ByteBuffer.wrap(e.toString().getBytes());
    }
    
    public List<List<Integer>> getStateTimes(ApplicationItem app) {
        List<TimedValue<ApplicationState>> tl = getTimeline(app);
        List<List<Integer>> l = new ArrayList<List<Integer>>();
        if (tl != null) {
            long lastTime = -1;
            long firstTime = -1;
            ApplicationState lastState = null;
            for (TimedValue<ApplicationState> p : tl) {
                if (lastState != null) {
                    l.add(new org.griphyn.vdl.karajan.Pair<Integer>(lastState.ordinal(), (int) (p.time - lastTime))); 
                }
                lastTime = p.time;
                lastState = p.value;
            }
        }
        return l;
    }
       
        
    private String getParam(Map<String, String> params, String name, String _default) {
        String value = params.get(name);
        if (value == null) {
            return _default;
        }
        else {
            return value;
        }
    }
    
    private String getParam(Map<String, String> params, String name) {
        String value = params.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Missing parameter '" + name + "'");
        }
        else {
            return value;
        }
    }

    public SortedMap<String, List<SortedSet<ApplicationItem>>> getByName() {
        return byName;
    }

    public Map<String, AppEntry> getEntries() {
        return entries;
    }

    public SystemState getState() {
        return state;
    }

    public List<ApplicationItem> getByTime() {
        return byTime;
    }

    public void writeEnabledStates(JSONEncoder e, String key) {
        e.writeMapKey(key);
        e.beginArray();
        for (ApplicationState s : ApplicationState.values()) {
            if (s.isEnabled()) {
                e.beginArrayItem();
                e.write(s.ordinal());
            }
        }
        e.endArray();
    }

    public Map<String, WorkerData> getWorkerData() {
        return workerData;
    }

    public Block getBlock(String id) {
        CoasterStatusItem item = (CoasterStatusItem) state.getItemByID(CoasterStatusItem.ID, StatefulItemClass.MISC); 
        return item.getBlocks().get(id);
    }

    public List<String> getWorkersByTime() {
        return workersByTime;
    }
    
    public void writePagingData(JSONEncoder e, int size, int page, int pageSize) {
        int pages = (int) Math.ceil(((double) size) / pageSize);
        e.writeMapItem("pages", pages);
        e.writeMapItem("hasPrev", page > 1);
        e.writeMapItem("hasNext", page < pages);
        e.writeMapItem("crtPage", page);
    }
}