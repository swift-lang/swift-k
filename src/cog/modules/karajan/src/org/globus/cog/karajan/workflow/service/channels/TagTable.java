//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TagTable {
	private static final long serialVersionUID = 1659255187618780167L;

	private Map map;
	private MutableInteger mkey;
	
	public TagTable() {
		map = new HashMap();
		mkey = new MutableInteger();
	}
	
	public synchronized boolean containsKey(int key) {
		mkey.setValue(key);
		return map.containsKey(mkey);
	}
	
	public synchronized void put(int key, Object value) {
		map.put(new MutableInteger(key), value);
	}
	
	public synchronized Object remove(int key) {
		mkey.setValue(key);
		return map.remove(mkey);
	}
	
	public synchronized Object get(int key) {
		mkey.setValue(key);
		return map.get(mkey);
	}
	
	public Collection values() {
		return map.values();
	}
	
	public String toString() {
		return map.toString();
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
	}

}
