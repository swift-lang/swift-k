//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 11, 2006
 */
package org.globus.cog.karajan.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DefList extends HashMap {
	private String name;
	
	public DefList(DefList prev) {
		super(4);
		super.putAll(prev);
		name = prev.name;
	}
	
	public DefList(String name) {
		super(4);
		this.name = name;
	}

	public synchronized void put(String prefix, Object def) {
		super.put(prefix, def);
	}
	
	public Collection prefixes() {
		return this.keySet();
	}
	
	public String getName() {
		return name;
	}
	
	public synchronized Entry first() {
		if (size() == 1) {
			return new Entry((Map.Entry) this.entrySet().iterator().next());
		}
		else {
			throw new NoSuchElementException("first");
		}
	}
	
	public synchronized Entry get(String prefix) {
		return new Entry(prefix, super.get(prefix));
	}
	
	public synchronized void removeAll(String prefix) {
		remove(prefix);
	}
	
	public synchronized boolean contains(String prefix) {
		return super.containsKey(prefix);
	}
	
	public static class Entry {
		private final String prefix;
		private Object def;
		
		public Entry(Map.Entry entry) {
			this.prefix = (String) entry.getKey();
			this.def = entry.getValue();
		}
		
		public Entry(String prefix, Object def) {
			this.prefix = prefix;
			this.def = def;
		}

		public Object getDef() {
			return def;
		}

		public String getPrefix() {
			return prefix;
		}

		public void setDef(Object def) {
			this.def = def;
		}
	}
}
