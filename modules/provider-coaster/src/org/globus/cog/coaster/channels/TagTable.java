//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.coaster.channels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagTable<T> {
	private static final long serialVersionUID = 1659255187618780167L;

	private Map<MutableInteger, T> map;
	private MutableInteger mkey;
	
	public TagTable() {
		map = new HashMap<MutableInteger, T>();
		mkey = new MutableInteger();
	}
	
	public synchronized boolean containsKey(int key) {
		mkey.setValue(key);
		return map.containsKey(mkey);
	}
	
	public synchronized void put(int key, T value) {
		map.put(new MutableInteger(key), value);
	}
	
	public synchronized T remove(int key) {
		mkey.setValue(key);
		return map.remove(mkey);
	}
	
	public synchronized T get(int key) {
		mkey.setValue(key);
		return map.get(mkey);
	}
	
	public Collection<T> values() {
		return map.values();
	}
	
	public Collection<Integer> keys() {
		List<Integer> l = new ArrayList<Integer>();
		for (MutableInteger i : map.keySet()) {
			l.add(i.getValue());
		}
		return l;
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public String toString() {
		return map.toString();
	}
	
	public void clear() {
	    map.clear();
	}
	
	private static class MutableInteger {
		private int value;
		
		public MutableInteger() {
			this(0);
		}
		
		public MutableInteger(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public void setValue(int value) {
			this.value = value;
		}

		public boolean equals(Object obj) {
			if (obj instanceof MutableInteger) {
				return ((MutableInteger) obj).value == value;
			}
			return false;
		}

		public int hashCode() {
			return value;
		}
		
		public String toString() {
		    return String.valueOf(value);
		}
	}
}
