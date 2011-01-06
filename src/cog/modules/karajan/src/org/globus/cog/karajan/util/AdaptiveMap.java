//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2006
 */
package org.globus.cog.karajan.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * <p>An implementation of a {@link java.util.Map} that tracks
 * typical required capacities and initializes new instances
 * with average sizes inferred from historical information.</p>
 * 
 * <p>The constructor allows initialization with different contexts,
 * each of which represents a different set of historical 
 * information</p>
 *  
 * 
 * @author Mihael Hategan
 *
 */
public class AdaptiveMap<K, V> implements Map<K, V> {
	private final Context context;
	private Map<K, V> map;

	public AdaptiveMap(Context context) {
		this.context = context;
		context.incMaps();
	}

	public V put(K key, V value) {
		createMap();
		V old = map.put(key, value);
		if (old == null) {
			context.incItems();
		}
		return old;
	}

	public void clear() {
		if (map != null) {
			map.clear();
		}
	}

	public boolean containsKey(Object key) {
		if (map == null) {
			return false;
		}
		else {
			return map.containsKey(key);
		}
	}

	public boolean containsValue(Object value) {
		if (map == null) {
			return false;
		}
		else {
			return map.containsValue(value);
		}
	}

	public Set<Map.Entry<K, V>> entrySet() {
		if (map == null) {
			return Collections.emptySet();
		}
		else {
			return map.entrySet();
		}
	}

	public boolean equals(Object o) {
		if (!(o instanceof Map<?, ?>)) {
			return false;
		}
		if (map == null) {
			return ((Map<?, ?>) o).isEmpty();
		}
		else {
			if (o instanceof AdaptiveMap<?, ?>) {
				return map.equals(((AdaptiveMap<?, ?>) o).map);
			}
			else {
				return map.equals(o);
			}
		}
	}

	public V get(Object key) {
		if (map == null) {
			return null;
		}
		else {
			return map.get(key);
		}
	}

	public int hashCode() {
		if (map == null) {
			return 0;
		}
		else {
			return map.hashCode();
		}
	}

	public boolean isEmpty() {
		return map == null || map.isEmpty();
	}

	public Set<K> keySet() {
		if (map == null) {
			return Collections.emptySet();
		}
		else {
			return map.keySet();
		}
	}

	public void putAll(Map<? extends K, ? extends V> t) {
		context.incItems(t.size());
		createMap();
		map.putAll(t);
	}

	public V remove(Object key) {
		if (map == null) {
			return null;
		}
		else {
			return map.remove(key);
		}
	}

	public int size() {
		if (map == null) {
			return 0;
		}
		else {
			return map.size();
		}
	}

	public Collection<V> values() {
		if (map == null) {
			return Collections.emptyList();
		}
		else {
			return map.values();
		}
	}

	private void createMap() {
		if (map == null) {
			map = new HashMap<K, V>(context.getSize());
		}
	}

	public String toString() {
		if (map == null) {
			return "{}";
		}
		else {
			return map.toString();
		}
	}

	public static class Context {
		private int maps, items;

		public Context() {
			maps = 0;
			items = 2;
		}

		public Context(int maps, int items) {
			this.maps = maps;
			this.items = items;
		}

		public int getSize() {
			int size = items * 4 / maps / 3 + 2;
			return size;
		}

		public synchronized void incMaps() {
			maps++;
		}

		public synchronized void incItems() {
			items++;
		}

		public synchronized void incItems(int count) {
			items += count;
		}
	}
}
