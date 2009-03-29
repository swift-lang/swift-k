/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor;

import java.util.Iterator;
import java.util.List;

import org.griphyn.vdl.karajan.monitor.items.StatefulItem;

public class StatefulItemClassSet {
	private RadixTree map;
	private Iterator i;
	private int crt;
	
	public StatefulItemClassSet() {
		map = new RadixTree();
	}

	public synchronized void add(StatefulItem item) {
		map.put(item.getID(), item);
		crt = Integer.MAX_VALUE;
	}

	public synchronized void remove(StatefulItem item) {
		map.remove(item.getID());
		crt = Integer.MAX_VALUE;
	}
	
	public synchronized StatefulItem getByID(String id) {
		return (StatefulItem) map.get(id);
	}
	
	public synchronized StatefulItem findWithPrefix(String prefix) {
		String key = map.find(prefix);
		if (key == null) {
			return null;
		}
		else {
			return (StatefulItem) map.get(key);
		}
	}
	
	public int size() {
		return map.size();
	}
	
	public synchronized List getAll() {
		return map.getAll();
	}
}
