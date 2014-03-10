// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 11, 2004
 */
package org.globus.cog.karajan.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is a simple/generic cache used by:
 * <ul>
 *  <li>{@link org.globus.cog.karajan.compiled.nodes.CacheNode}, which is the implementation of sys:cache</li>
 *  <li>{@link org.globus.cog.karajan.compiled.nodes.Once}</li>
 * </ul>
 */
public class Cache implements Serializable {
	private static final long serialVersionUID = -8944421098726077701L;
	
	private final static int ENTRY_POOL_MAX_SIZE = 25;
	private final static int MAX_CACHE_SIZE = 1000;
	private final static int CACHE_PURGE_SIZE = 100;
	
	private HashMap<Object, Value> store;
	
	private int maxCacheSize = MAX_CACHE_SIZE;
	
	public Cache() {
		store = new HashMap<Object, Value>();
	}

	public Object getCachedValue(Object key) {
		Value value = store.get(key);
		if (value != null) {
			value.hits++;
			value.lastHit = System.currentTimeMillis();
			return value.object;
		}
		return null;
	}

	public boolean isCached(Object key) {
		return store.containsKey(key);
	}

	public void addValue(Object key, Object value) {
		store.put(key, new Value(value));
		checkSize();
	}
	
	public void addAndLock(Object key, Object value) {
		Value v = new Value(value);
		v.locked = true;
		store.put(key, v);
		checkSize();
	}
	
	public void unlock(Object key) {
		Value v = store.get(key);
		v.locked = false;
	}

	private void checkSize() {
		if (maxCacheSize > 0 && store.size() > maxCacheSize) {
			long[] scores = new long[store.size()];
			int index = 0;
			for (Value v : store.values()) {
				if (!v.locked) {
					scores[index++] = score(v.hits, v.lastHit);
				}
			}
			Arrays.sort(scores);
			long threshold = scores[CACHE_PURGE_SIZE];
			Iterator<Map.Entry<Object, Value>> j = store.entrySet().iterator();
			while (j.hasNext()) {
				Map.Entry<Object, Value> e = j.next();
				Value v = e.getValue();
				if (!v.locked && score(v.hits, v.lastHit) < threshold) {
					j.remove();
				}
			}
		}
	}

	private long score(int hits, long lastHit) {
		return hits + lastHit / 10000;
	}
	
	public int getMaxCacheSize() {
		return maxCacheSize;
	}

	public void setMaxCacheSize(int maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}

	private static class Value {

		public int hits;
		public long lastHit;
		public boolean locked;
		public Object object;

		public Value(Object object) {
			hits = 0;
			lastHit = System.currentTimeMillis();
			this.object = object;
		}
	}

	public interface Cacheable {

		/*
		 * This allows objects to implement their own way of verifying whether
		 * they are cached or not. The method should return null if it results
		 * in a hit. Otherwise, it should return some metadata that can be used
		 * for further verification, and which will be passed as an argument at
		 * the next verification.
		 */
		Object verify(Object stored);
	}
}