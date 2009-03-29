/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.stack.VariableStack;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;

public class SystemState {
	private Map classes;
    private Set listeners;
    private Map stats;
    private int total, completed;
    private long start;
    private VariableStack stack;
    private String projectName;

	public SystemState(String projectName) {
	    this.projectName = projectName;
		classes = new HashMap();
        listeners = new HashSet();
        stats = new HashMap();
	}

	public void addItem(StatefulItem item) {
		getItemClassSet(item.getItemClass()).add(item);
        notifyListeners(SystemStateListener.ITEM_ADDED, item);
	}

	public StatefulItemClassSet getItemClassSet(StatefulItemClass cls) {
        StatefulItemClassSet clset;
		synchronized (classes) {
			clset = (StatefulItemClassSet) classes.get(cls);
            if (clset == null) {
            	clset = new StatefulItemClassSet();
                classes.put(cls, clset);
            }
		}
        return clset;
	}

	public void removeItem(StatefulItem item) {
		getItemClassSet(item.getItemClass()).remove(item);
        notifyListeners(SystemStateListener.ITEM_REMOVED, item);
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
		notifyListeners(SystemStateListener.ITEM_UPDATED, item);
	}
    
    protected void notifyListeners(int updateType, StatefulItem item) {
    	Iterator i = listeners.iterator();
        while (i.hasNext()) {
        	((SystemStateListener) i.next()).itemUpdated(updateType, item);
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
            Stats s = (Stats) stats.get(key);
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

    public VariableStack getStack() {
        return stack;
    }

    public void setStack(VariableStack stack) {
        this.stack = stack;
    }

    public String getProjectName() {
        return projectName;
    }
}
