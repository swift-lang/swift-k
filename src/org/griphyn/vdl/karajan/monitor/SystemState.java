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

import k.rt.Stack;

import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;

public class SystemState {
	private Map<StatefulItemClass, StatefulItemClassSet<? extends StatefulItem>> classes;
    private Set<SystemStateListener> listeners;
    private Map<String, Stats> stats;
    private int total, completed;
    private long start;
    private Stack stack;
    private String projectName;

	public SystemState(String projectName) {
	    this.projectName = projectName;
		classes = new HashMap<StatefulItemClass, StatefulItemClassSet<? extends StatefulItem>>();
        listeners = new HashSet<SystemStateListener>();
        stats = new HashMap<String, Stats>();
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
}
