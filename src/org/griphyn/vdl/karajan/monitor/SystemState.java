/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import k.rt.Stack;

import org.griphyn.vdl.karajan.monitor.common.GlobalTimer;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.SummaryItem;

public class SystemState {
	private Map<StatefulItemClass, StatefulItemClassSet<? extends StatefulItem>> classes;
    private Set<SystemStateListener> listeners;
    private Map<String, Stats> stats;
    private int total, completed;
    private long start, currentTime;
    private Stack stack;
    private String projectName;
    private final Runtime runtime;
    
    private static final Unit BYTES = new Unit.P2("B");

	public SystemState(String projectName) {
	    this.projectName = projectName;
		classes = new HashMap<StatefulItemClass, StatefulItemClassSet<? extends StatefulItem>>();
        listeners = new HashSet<SystemStateListener>();
        stats = new HashMap<String, Stats>();
        runtime = Runtime.getRuntime();
        addItem(new SummaryItem());
        GlobalTimer.getTimer().schedule(new TimerTask() {
            public void run() {
                update();
            }
        }, 1000, 1000);
	}
	
	protected void update() {
	    
	}

	public void addItem(StatefulItem item) {
		getItemClassSet(item.getItemClass()).add(item);
        notifyListeners(SystemStateListener.UpdateType.ITEM_ADDED, item);
	}

	@SuppressWarnings("unchecked")
    public <T extends StatefulItem> StatefulItemClassSet<T> getItemClassSet(StatefulItemClass cls) {
        StatefulItemClassSet<T> clset;
		synchronized (classes) {
			clset = (StatefulItemClassSet<T>) classes.get(cls);
            if (clset == null) {
            	clset = new StatefulItemClassSet<T>();
                classes.put(cls, clset);
            }
		}
        return clset;
	}

	public void removeItem(StatefulItem item) {
		getItemClassSet(item.getItemClass()).remove(item);
        notifyListeners(SystemStateListener.UpdateType.ITEM_REMOVED, item);
	}

	public void setParent(StatefulItem item, StatefulItem parent) {
		item.setParent(parent);
	}

	public StatefulItem getItemByID(String id, StatefulItemClass cls) {
		return getItemClassSet(cls).getByID(id);
	}
	
	public StatefulItem find(String id, StatefulItemClass cls) {
		return getItemClassSet(cls).findWithPrefix(id);
	}

	public void itemUpdated(StatefulItem item) {
		notifyListeners(SystemStateListener.UpdateType.ITEM_UPDATED, item);
	}
    
    protected void notifyListeners(SystemStateListener.UpdateType updateType, StatefulItem item) {
        for (SystemStateListener l : listeners) {
            l.itemUpdated(updateType, item);
        }
    }
    
    public void addListener(SystemStateListener l) {
    	listeners.add(l);
    }
    
    public void removeListener(SystemStateListener l) {
    	listeners.remove(l);
    }

    public Stats getStats(String key) {
        synchronized(stats) {
            Stats s = stats.get(key);
            if (s == null) {
                s = new Stats();
                stats.put(key, s);
            }
            return s;
        }
    }

    public synchronized void incTotal() {
        if (total == 0) {
            start = System.currentTimeMillis();
        }
        total++;
    }

    public synchronized void incCompleted() {
        completed++;
    }

    public int getTotal() {
        return total;
    }

    public int getCompleted() {
        return completed;
    }

    public long getStart() {
        return start;
    }

    public Stack getStack() {
        return stack;
    }

    public void setStack(Stack stack) {
        this.stack = stack;
    }

    public String getProjectName() {
        return projectName;
    }
    
    public String getMaxHeapFormatted() {
        return BYTES.format(getMaxHeap());
    }
    
    public String getCurrentHeapFormatted() {
        return BYTES.format(getCurrentHeap());
    }
    
    public String getElapsedTimeFormatted() {
        return format(System.currentTimeMillis() - start);
    }
    
    public String getEstimatedTimeLeftFormatted() {
        if (completed == 0) {
            return "N/A";
        }
        else {
            long time = System.currentTimeMillis() - start;
            long et = (time * total / completed) - time;
            return format(et);
        }
    }
    
    public String getGlobalProgressString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Est. progress: ");
        if (total != 0) {
            sb.append(completed * 100 / total);
            sb.append("%");
        }
        else {
            sb.append("N/A");
        }
        sb.append("   Elapsed time: ");
        sb.append(getElapsedTimeFormatted());
        sb.append("   Est. time left: ");
        if (total != 0 && completed != 0) {
            sb.append(getEstimatedTimeLeftFormatted());
        }
        else {
            sb.append("N/A");
        }
        return sb.toString();
    }

    public static String format(long v) {
        v = v / 1000;
        StringBuffer sb = new StringBuffer();
        int h = (int) (v / 3600);
        if (h < 10) {
            sb.append('0');
        }
        sb.append(v / 3600);
        sb.append(':');
        int m = (int) ((v % 3600) / 60);
        if (m < 10) {
            sb.append('0');
        }
        sb.append(m);
        sb.append(':');
        int s = (int) (v % 60);
        if (s < 10) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString();
    }

    
    public long getMaxHeap() {
        return runtime.maxMemory();
    }
    
    public long getCurrentHeap() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
}
