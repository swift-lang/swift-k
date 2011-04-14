/*
 * Created on Jul 15, 2008
 */
package org.griphyn.vdl.karajan.lib.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CacheMapAdapter implements Map {
	private VDLFileCache cache;
	
	public CacheMapAdapter(VDLFileCache cache) {
		this.cache = cache;
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key) {
		throw new UnsupportedOperationException();
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public Set entrySet() {
		throw new UnsupportedOperationException();
	}

	public Object get(Object key) {
		return cache.getPaths(key);
	}

	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	public Set keySet() {
		throw new UnsupportedOperationException();
	}

	public Object put(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map t) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		throw new UnsupportedOperationException();
	}

	public Collection values() {
		throw new UnsupportedOperationException();
	}
	
	public String toString() {
	    return cache.toString();
	}
}
