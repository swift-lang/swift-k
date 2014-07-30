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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;
import org.griphyn.vdl.karajan.monitor.monitors.http.BrowserDataBuilder.TimedValue;

public class AppDetailBuilder {

    private String name;
    private SortedMap<String, List<SortedSet<ApplicationItem>>> byName;
    private BrowserDataBuilder db;

    public AppDetailBuilder(BrowserDataBuilder db, String name) {
        this.db = db;
        this.byName = db.getByName();
        this.name = name;
    }
    
    private static class StateTimesAverage {
        public static final int N = 7;
        public int[] stateTimeSum = new int[N];
        public int[] stateTimeCount = new int[N];
        
        public void add(ApplicationState state, long time) {
            int index = INDEX_MAPPING[state.ordinal()];
            stateTimeCount[index]++;
            stateTimeSum[index] += (int) time;
        }
        
        public int[] getAverages() {
            int[] avg = new int[N];
            for (int i = 0; i < N; i++) {
                if (stateTimeCount[i] == 0) {
                    avg[i] = 0;
                }
                else {
                    avg[i] = stateTimeSum[i] / stateTimeCount[i];
                }
            }
            return avg;
        }
    }
    
    private static int[] INDEX_MAPPING = new int[ApplicationState.values().length];
    
    static {
        INDEX_MAPPING[ApplicationState.INITIALIZING.ordinal()] = 0; 
        INDEX_MAPPING[ApplicationState.SELECTING_SITE.ordinal()] = 1;
        INDEX_MAPPING[ApplicationState.SUBMITTING.ordinal()] = 2;
        INDEX_MAPPING[ApplicationState.SUBMITTED.ordinal()] = 3;
        INDEX_MAPPING[ApplicationState.STAGE_IN.ordinal()] = 4;
        INDEX_MAPPING[ApplicationState.ACTIVE.ordinal()] = 5;
        INDEX_MAPPING[ApplicationState.STAGE_OUT.ordinal()] = 6;
    }


    public void getData(JSONEncoder e) {
        // sites it ran on
        Set<String> sites = new TreeSet<String>();
        // average times for each relevant state then for total
        // relevant states: Initializing, Sel. site, Stage in, Submitting, Submitted (queued remotely).
        // Active, Stage out, Total
        StateTimesAverage st = new StateTimesAverage();
        // same for each site
        Map<String, StateTimesAverage> sts = new HashMap<String, StateTimesAverage>();
        List<Integer> totalTimesC = new ArrayList<Integer>();
        List<Integer> totalTimesF = new ArrayList<Integer>();
        Map<String, List<Integer>> totalTimesCompletedSite = new HashMap<String, List<Integer>>();
        Map<String, List<Integer>> totalTimesFailedSite = new HashMap<String, List<Integer>>();
        
        int count = 0;
        List<SortedSet<ApplicationItem>> l = byName.get(name);
        for (int i = 0; i < l.size(); i++) {
            SortedSet<ApplicationItem> ss = l.get(i);
            for (ApplicationItem item : ss) {
                count++;
                String host = item.getHost();
                if (!sites.contains(host)) {
                    sites.add(host);
                    sts.put(host, new StateTimesAverage());
                    totalTimesCompletedSite.put(host, new ArrayList<Integer>());
                    totalTimesFailedSite.put(host, new ArrayList<Integer>());
                }
                
                StateTimesAverage stss = sts.get(host);
                List<Integer> ttCs = totalTimesCompletedSite.get(host);
                List<Integer> ttFs = totalTimesFailedSite.get(host);
                
                List<TimedValue<ApplicationState>> tl = db.getTimeline(item);
                long lastTime = -1;
                long firstTime = -1;
                ApplicationState lastState = null;
                for (TimedValue<ApplicationState> p : tl) {
                    if (lastState != null) {
                        switch (lastState) {
                            case STAGE_IN:
                                firstTime = p.time;
                            case FINISHED_SUCCESSFULLY:
                            case FAILED:
                            case INITIALIZING:
                            case SELECTING_SITE:
                            case SUBMITTING:
                            case SUBMITTED:
                            case ACTIVE:
                            case STAGE_OUT:
                                st.add(lastState, p.time - lastTime);
                                stss.add(lastState, p.time - lastTime);
                        }
                        int time;
                        switch (lastState) {
                            case FINISHED_SUCCESSFULLY:
                                time = (int) (p.time - firstTime);
                                totalTimesC.add(time);
                                ttCs.add(time);
                                break;
                            case FAILED:
                                time = (int) (p.time - firstTime);
                                totalTimesF.add(time);
                                ttFs.add(time);
                                break;
                        }
                    }
                    lastTime = p.time;
                    lastState = p.value;
                }
            }
        }
        
        // get range for total times
        int minTime = Math.min(min(totalTimesC), min(totalTimesF));
        int maxTime = Math.max(max(totalTimesC), max(totalTimesF));
        
        
        int bins = (int) Math.max(Math.min(count / 5, 100.0), 1);
        double binSize = ((double) (maxTime - minTime)) / bins;
        
        // now serialize this
        e.beginMap();
            e.writeMapItem("name", name);
            e.writeMapItem("count", count);
            e.writeMapItem("completedCount", totalTimesC.size());
            e.writeMapItem("failedCount", totalTimesF.size());
            e.writeMapItem("avgStateTimes", st.getAverages());
            e.writeMapItem("distMinTime", minTime);
            e.writeMapItem("distMaxTime", maxTime);
            e.writeMapItem("bins", 100);
            e.writeMapItem("completedTimeDist", bin(totalTimesC, binSize, minTime, maxTime, bins));
            e.writeMapItem("failedTimeDist", bin(totalTimesF, binSize, minTime, maxTime, bins));
            e.writeMapItem("completedTimeAvg", avg(totalTimesC));
            e.writeMapItem("failedTimeAvg", avg(totalTimesF));
            e.writeMapKey("sites");
            e.beginArray();
                for (String host : sites) {
                    e.beginArrayItem();
                        e.beginMap();
                            e.writeMapItem("name", host);
                            e.writeMapItem("count", totalTimesCompletedSite.get(host).size());
                            e.writeMapItem("completedCount", totalTimesCompletedSite.get(host).size());
                            e.writeMapItem("failedCount", totalTimesFailedSite.get(host).size());
                            e.writeMapItem("avgStateTimes", sts.get(host).getAverages());
                            e.writeMapItem("distMinTime", minTime);
                            e.writeMapItem("distMaxTime", maxTime);
                            e.writeMapItem("completedTimeDist", bin(totalTimesCompletedSite.get(host), binSize, minTime, maxTime, bins));
                            e.writeMapItem("failedTimeDist", bin(totalTimesFailedSite.get(host), binSize, minTime, maxTime, bins));
                            e.writeMapItem("completedTimeAvg", avg(totalTimesCompletedSite.get(host)));
                            e.writeMapItem("failedTimeAvg", avg(totalTimesFailedSite.get(host)));
                        e.endMap();
                    e.endArrayItem();
                }
            e.endArray();
            
        e.endMap();
    }

    private int avg(List<Integer> l) {
        if (l.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (Integer i : l) {
            sum += i;
        }
        return sum / l.size();
    }

    private List<Integer> bin(List<Integer> l, double binSize, int minTime, int maxTime, int binCount) {
        List<Integer> hist = new ArrayList<Integer>();
        for (int i = 0; i < binCount; i++) {
            hist.add(0);
        }
        for (Integer v : l) {
            int bin = (int) Math.ceil((v - minTime) / binSize) - 1;
            hist.set(bin, hist.get(bin) + 1);
        }
        return hist;
    }
    
    private int min(List<Integer> l) {
        if (l.isEmpty()) {
            return 0;
        }
        
        int min = l.get(0);
        for (Integer i : l) {
            if (i < min) {
                min = i;
            }
        }
        return min;
    }
    
    private int max(List<Integer> l) {
        if (l.isEmpty()) {
            return 0;
        }
        
        int max = l.get(0);
        for (Integer i : l) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }
}
