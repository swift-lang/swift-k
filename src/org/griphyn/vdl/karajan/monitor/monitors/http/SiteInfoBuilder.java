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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;

public class SiteInfoBuilder {

    private BrowserDataBuilder db;
    private List<ApplicationItem> byTime;
    private List<Integer> enabledStatesMapping;

    public SiteInfoBuilder(BrowserDataBuilder db) {
        this.db = db;
        this.byTime = db.getByTime();
        
        enabledStatesMapping = new ArrayList<Integer>();
        for (ApplicationState state : ApplicationState.values()) {
            if (state.isEnabled()) {
                enabledStatesMapping.add(state.ordinal());
            }
        }
    }
    
    private static class SiteInfo {
        public String name;
        public Map<String, List<Integer>> appStates;
        public Map<String, List<Integer>> appCummulativeStateTimes;
        public Map<String, Integer> appCountByType;
        public SortedSet<String> workers;
        public int appCount;
        
        public SiteInfo(String name) {
            this.name = name;
            this.appStates = new TreeMap<String, List<Integer>>();
            this.appCountByType = new TreeMap<String, Integer>();
            this.appCummulativeStateTimes = new TreeMap<String, List<Integer>>();
            this.workers = new TreeSet<String>();
        }
    }
   

    public void getData(JSONEncoder e) {
        /*
         * List of sites
         * List of app names it ran with state counts and average times
         * List of workers
         */
        
        Map<String, SiteInfo> si = new HashMap<String, SiteInfo>();
        
        for (ApplicationItem app : byTime) {
            addApp(getOrCreateSite(si, app.getHost()), app);
        }
        
        e.beginMap();
        db.writeEnabledStates(e, "enabledStates");
        e.writeMapKey("sites");
        e.beginArray();
            for (SiteInfo i : si.values()) {
                e.beginArrayItem();
                e.beginMap();
                    e.writeMapItem("name", i.name);
                    e.writeMapItem("appCount", i.appCount);
                    e.writeMapItem("workers", i.workers);
                    e.writeMapKey("stateCounts");
                        e.beginMap();
                            for (Map.Entry<String, List<Integer>> e1 : i.appStates.entrySet()) {
                                e.writeMapKey(e1.getKey());
                                e.beginArray();
                                    int index = 0;
                                    for (Integer i2 : e1.getValue()) {
                                        if (ApplicationState.values()[index++].isEnabled()) {
                                            e.writeArrayItem(i2.intValue());
                                        }
                                    }
                                e.endArray();
                            }
                        e.endMap();
                    e.writeMapKey("avgStateTimes");
                        e.beginMap();
                            for (Map.Entry<String, List<Integer>> e1 : i.appCummulativeStateTimes.entrySet()) {
                                e.writeMapKey(e1.getKey());
                                int count = i.appCountByType.get(e1.getKey());
                                e.beginArray();
                                    int index = 0;
                                    for (Integer i2 : e1.getValue()) {
                                        if (ApplicationState.values()[index++].isEnabled()) {
                                            e.writeArrayItem(i2.intValue() / count);
                                        }
                                    }
                                e.endArray();
                            }
                        e.endMap();
                e.endMap();
            }
        e.endArray();
        e.endMap();
    }
    
    private void addApp(SiteInfo si, ApplicationItem app) {
        si.appCount++;
        if (app.getWorkerId() != null) {
            si.workers.add(app.getWorkerId());
        }
        String name = app.getName();
        
        List<Integer> states = si.appStates.get(name);
        List<Integer> stateTimes = si.appCummulativeStateTimes.get(name);
        Integer count = si.appCountByType.get(name);
        if (states == null) {
            states = new ArrayList<Integer>();
            states.addAll(Collections.nCopies(ApplicationState.values().length, 0));
            si.appStates.put(name, states);
            
            stateTimes = new ArrayList<Integer>();
            stateTimes.addAll(Collections.nCopies(ApplicationState.values().length, 0));
            si.appCummulativeStateTimes.put(name, stateTimes);
            
            count = 0;
        }
        
        add(states, app.getState().ordinal(), 1);
        List<List<Object>> st = db.getStateTimes(app);
        for (List<Object> sti : st) {
            add(stateTimes, (Integer) sti.get(0), (Integer) sti.get(1));
        }
        si.appCountByType.put(name, count + 1);
    }

    private void add(List<Integer> l, int index, int amount) {
        int crt = l.get(index);
        l.set(index, crt + amount);
    }

    private SiteInfo getOrCreateSite(Map<String, SiteInfo> si, String host) {
        SiteInfo s = si.get(host);
        if (s == null) {
            s = new SiteInfo(host);
            si.put(host, s);
        }
        return s;
    }

}
