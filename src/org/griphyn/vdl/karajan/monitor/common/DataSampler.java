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
 * Created on Jul 31, 2013
 */
package org.griphyn.vdl.karajan.monitor.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.TreeMap;

import org.globus.cog.coaster.channels.PerformanceDiagnosticInputStream;
import org.globus.cog.coaster.channels.PerformanceDiagnosticOutputStream;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.Unit;
import org.griphyn.vdl.karajan.monitor.items.AbstractStatefulItem;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.SummaryItem;
import org.griphyn.vdl.karajan.monitor.processors.coasters.CoasterStatusItem;

public class DataSampler extends AbstractStatefulItem {
    public static final String ID = "SAMPLER";
    
    public static final int CAPACITY = 4 * 60 * 60; // 4 hours
    
    private SystemState state;
    private Map<String, List<Series<? extends Number>>> categories;
    private Map<String, Series<? extends Number>> data;
    private int count;
    private long offset;
    private List<Listener> listeners;
    
    public interface Listener {
        void dataItemAdded();
    }
        
    public DataSampler(SystemState state) {
        super(ID);
        this.state = state;
        
        this.categories = new TreeMap<String, List<Series<? extends Number>>>();
        this.data = new HashMap<String, Series<? extends Number>>();
        this.offset = -1;
        this.count = 0;
        this.listeners = new ArrayList<Listener>();
        initializeData();
        
        state.schedule(new TimerTask() {
            @Override
            public void run() {
                sample();
            }
        }, 1000, 1000);
    }
    
    @Override
    public StatefulItemClass getItemClass() {
        return StatefulItemClass.WORKFLOW;
    }
    
    public void addListener(Listener l) {
        listeners.add(l);
    }
    
    public void removeListener(Listener l) {
        listeners.add(l);
    }
    
    private static final Unit COUNT = new Unit.Fixed("Count");
    private static final Unit BYTES = new Unit.P2("B");

    private void initializeData() {
        SummaryItem summary = (SummaryItem) state.getItemByID(SummaryItem.ID, StatefulItemClass.WORKFLOW);
        data = new HashMap<String, Series<? extends Number>>();
        for (ApplicationState state : SummaryItem.STATES) {
            addSeries("Application States", 
                new Series<Integer>("apps/" + state.getName(), state.getName(), COUNT, 
                        new SummaryItemSampler(summary, state)));
        }
        
        addSeries("Java Virtual Machine", 
            new Series<Long>("jvm/heapUsed", "JVM Heap Used", BYTES, 
                new ReflectionSampler<Long>(state, "getUsedHeap")),
            new Series<Integer>("jvm/activeThreads", "JVM Active Threads", COUNT,
                new ReflectionSampler<Integer>(state, "getCurrentThreads")));
        
        
        CoasterStatusItem coaster = (CoasterStatusItem) state.getItemByID(CoasterStatusItem.ID, StatefulItemClass.MISC);
        addSeries("Coasters",
            new Series<Integer>("coasters/requestedCores", "Requested Cores", COUNT,
                 new ReflectionSampler<Integer>(coaster, "getRequestedCores")),
            new Series<Integer>("coasters/activeCores", "Active Cores", COUNT,
                 new ReflectionSampler<Integer>(coaster, "getActiveCores")),
            new Series<Integer>("coasters/doneCores", "Done Cores", COUNT,
                 new ReflectionSampler<Integer>(coaster, "getDoneCores")),
            new Series<Integer>("coasters/failedCores", "Failed Cores", COUNT,
                 new ReflectionSampler<Integer>(coaster, "getFailedCores")),
            new Series<Integer>("coasters/queuedBlocks", "Queued Blocks", COUNT,
                 new ReflectionSampler<Integer>(coaster, "getQueuedBlocks")),
            new Series<Integer>("coasters/activeBlocks", "Active Blocks", COUNT,
                 new ReflectionSampler<Integer>(coaster, "getActiveBlocks")),
            new Series<Integer>("coasters/doneBlocks", "Done Blocks", COUNT,
                 new ReflectionSampler<Integer>(coaster, "getDoneBlocks")),
            new Series<Integer>("coasters/failedBlocks", "Failed Blocks", COUNT,
                 new ReflectionSampler<Integer>(coaster, "getFailedBlocks")));
        
        System.setProperty("tcp.channel.log.io.performance", "true");
        addSeries("Coaster I/O",
            new Series<Integer>("coasterio/writeRate", "Write Throughput", BYTES,
                 new ReflectionSampler<Integer>(PerformanceDiagnosticOutputStream.class, "getCurrentRate")),
            new Series<Integer>("coasterio/avgWriteRate", "Write Throughput (avg.)", BYTES,
                 new ReflectionSampler<Integer>(PerformanceDiagnosticOutputStream.class, "getAverageRate")),
            new Series<Integer>("coasterio/bytesWritten", "Bytes Written", BYTES,
                 new ReflectionSampler<Integer>(PerformanceDiagnosticOutputStream.class, "getTotal")),
            new Series<Integer>("coasterio/readRate", "Read Throughput", BYTES,
                 new ReflectionSampler<Integer>(PerformanceDiagnosticInputStream.class, "getCurrentRate")),
            new Series<Integer>("coasterio/avgReadRate", "Read Throughput (avg.)", BYTES,
                 new ReflectionSampler<Integer>(PerformanceDiagnosticInputStream.class, "getAverageRate")),
            new Series<Integer>("coasterio/bytesRead", "Bytes Read", BYTES,
                 new ReflectionSampler<Integer>(PerformanceDiagnosticInputStream.class, "getTotal")));
    }
    
    private void addSeries(String category, Series<?>... ss) {
        for (Series<?> s : ss) {
            data.put(s.getKey(), s);
            List<Series<?>> l = categories.get(category);
            if (l == null) {
                l = new ArrayList<Series<?>>();
                categories.put(category, l);
            }
            l.add(s);
        }
    }

    public long getOffset() {
        return offset;
    }
    
    public int getCapacity() {
        return CAPACITY;
    }

    protected void sample() {
        long now = state.getCurrentTime() / 1000;
        if (offset + count != now) {
            if (offset < 0) {
                offset = now;
            }
            
            for (Map.Entry<String, Series<? extends Number>> e : data.entrySet()) {
                e.getValue().sample();
            }
            
            for (Listener l : listeners) {
                l.dataItemAdded();
            }
        }
    }
    
    public Collection<Series<? extends Number>> getAllSeries() {
        return data.values();
    }
    
    public Map<String, List<Series<? extends Number>>> getCategories() {
        return categories;
    }
    
    public Series<? extends Number> getSeries(String key) {
        return data.get(key);
    }
    
    public static class Series<T extends Number> {
        private final String key;
        private final String label;
        private final Unit unit;
        private final SeriesSampler<T> sampler;
        private ArrayList<T> data;
        private T maxValue, minValue;
        
        
        public Series(String key, String label, Unit unit, SeriesSampler<T> sampler) {
            this.key = key;
            this.label = label;
            this.unit = unit;
            this.sampler = sampler;
            data = new ArrayList<T>();
        }        
               
        public String getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }

        public Unit getUnit() {
            return unit;
        }

        public ArrayList<T> getData() {
            return data;
        }
        
        public void sample() {
            addData(sampler.sample());
        }

        private void addData(T value) {
            checkCapacity();
            if (maxValue == null) {
                maxValue = value;
                minValue = value;
            }
            else {
                if (maxValue.doubleValue() < value.doubleValue()) {
                    maxValue = value;
                }
                if (minValue.doubleValue() > value.doubleValue()) {
                    minValue = value;
                }
            }
            data.add(value);
        }

        private void checkCapacity() {
            if (data.size() > CAPACITY * 3 / 2) {
                int sz = this.data.size();
                ArrayList<T> newData = new ArrayList<T>(this.data.subList(sz - CAPACITY, sz));
                this.data = newData;
            }
        }

        public T getMaxValue() {
            return maxValue;
        }

        public T getMinValue() {
            return minValue;
        }
    }
    
    private static abstract class SeriesSampler<T extends Number> {
        abstract T sample();
    }
    
    private static class SummaryItemSampler extends SeriesSampler<Integer> {
        private SummaryItem item;
        private ApplicationState state;
        
        public SummaryItemSampler(SummaryItem item, ApplicationState state) {
            if (item == null) {
                throw new NullPointerException();
            }
            this.item = item;
            this.state = state;
        }

        @Override
        Integer sample() {
            return item.getCount(state);
        }
    }
    
    private static class ReflectionSampler<T extends Number> extends SeriesSampler<T> {
        private Object obj;
        private String getterName;
        private Method method;
        
        public ReflectionSampler(Object obj, String getterName) {
            this.getterName = getterName;
            
            if (obj.getClass().equals(Class.class)) {
                // static
                this.obj = null;
                try {
                    this.method = ((Class<?>) obj).getMethod(getterName, new Class<?>[0]);
                }
                catch (Exception e) {
                    throw new IllegalArgumentException("Cannot access method " + getterName, e);
                }
            }
            else {
                this.obj = obj;
                try {
                    this.method = obj.getClass().getMethod(getterName, new Class<?>[0]);
                }
                catch (Exception e) {
                    throw new IllegalArgumentException("Cannot access method " + getterName, e);
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        T sample() {
            try {
                return (T) method.invoke(obj);
            }
            catch (Exception e) {
                return null;
            }
        }
    }

    public static void install(SystemState state) {
        state.addItem(new DataSampler(state));
    }
}
