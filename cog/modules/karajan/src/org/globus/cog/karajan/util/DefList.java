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
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

public class DefList {
	private final DefList prev;
	private final String name;
	private String[] prefixes;
	private Object[] defs;
	private int count; 
	private int size;

	public DefList(DefList prev) {
		init(1);
		name = prev.name;
		this.prev = prev;
		if (prev != null) {
			size = prev.size;
		}
	}

	public DefList(String name) {
		init(2);
		this.name = name;
		this.prev = null;
	}

	private void init(int size) {
		prefixes = new String[2];
		defs = new Object[2];
	}

	private void resize(int newSize) {
		String[] newPrefixes = new String[newSize];
		Object[] newDefs = new Object[newSize];
		System.arraycopy(prefixes, 0, newPrefixes, 0, prefixes.length);
		System.arraycopy(defs, 0, newDefs, 0, defs.length);
	}

	public synchronized void put(String prefix, Object def) {
		if (!replace(prefix, def)) {
			if (get(prefix) == null) {
				size++;
			}
			if (prefixes.length == count) {
				resize(count * 2);
			}
			prefixes[count] = prefix;
			defs[count] = def;
			count++;
		}
	}

	protected boolean replace(String prefix, Object def) {
		for (int i = 0; i < count; i++) {
			if ((prefix == null && prefixes[i] == null)
					|| (prefix != null && prefix.equals(prefixes[i]))) {
				defs[i] = def;
				return true;
			}
		}
		return false;
	}
	
	private void fillPrefixes(Set s) {
		for (int i = 0; i < count; i++) {
			s.add(prefixes[i]);
		}
		if (prev != null) {
			prev.fillPrefixes(s);
		}
	}

	private int fillPrefixes(String[] p) {
		int crt;
		if (prev == null) {
			crt = 0;
		}
		else {
			crt = prev.fillPrefixes(p);
		}
		System.arraycopy(prefixes, 0, p, crt, count);
		return crt + count;
	}

	public Collection prefixes() {
		Set s = new HashSet();
		fillPrefixes(s);
		return s;
	}

	public String[] currentPrefixes() {
		String[] p = new String[count];
		System.arraycopy(prefixes, 0, p, 0, count);
		return p;
	}

	public String getName() {
		return name;
	}

	public synchronized Entry first() {
		if (size == 1) {
			if (count == 0) {
				return prev.first();
			}
			else {
				return new Entry(name, prefixes[0], defs[0]);
			}
		}
		else {
			throw new NoSuchElementException("first");
		}
	}

	public int size() {
		return size;
	}

	public synchronized Object get(String prefix) {
		for (int i = 0; i < count; i++) {
			if ((prefix == null && prefixes[i] == null)
					|| (prefix != null && prefix.equals(prefixes[i]))) {
				return defs[i];
			}
		}
		if (prev != null) {
			return prev.get(prefix);
		}
		else {
			return null;
		}
	}

	public synchronized boolean contains(String prefix) {
		return get(prefix) != null;
	}

	public static class Entry {
		private String name;
		private String prefix;
		private Object def;

		public Entry(String name, String prefix, Object def) {
			this.name = name;
			this.prefix = prefix;
			this.def = def;
		}
		
		public Entry(String name, Object def) {
			this.name = name;
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

		public String toString() {
			return getFullName() + " -> " + def;
		}

		public String getFullName() {
			if (prefix != null && prefix.length() > 0) {
				return prefix + ":" + name;
			}
			else {
				return name;
			}
		}
	}
}
