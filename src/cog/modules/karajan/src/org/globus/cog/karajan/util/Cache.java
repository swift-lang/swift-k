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

/**
 * This is a simple/generic cache used by:
 * <ul>
 *  <li>{@link org.globus.cog.karajan.workflow.nodes.CacheNode}, which is the implementation of sys:cache</li>
 *  <li>{@link org.globus.cog.karajan.workflow.nodes.Once}</li>
 * </ul>
 */
public class Cache implements Serializable {
	private static final long serialVersionUID = -8944421098726077701L;
	
	private final static int ENTRY_POOL_MAX_SIZE = 25;
	private final static int MAX_CACHE_SIZE = 1000;
	private final static int CACHE_PURGE_SIZE = 100;
	private HashMap primitives;
	
	private int maxCacheSize = MAX_CACHE_SIZE;
	
	public Cache() {
		primitives = new HashMap();
	}

	public Object getCachedValue(Object key) {
		Value value = (Value) primitives.get(key);
		if (value != null) {
			value.hits++;
			value.lastHit = System.currentTimeMillis();
			return value.object;
		}
		return null;
	}

	public boolean isCached(Object key) {
		return primitives.containsKey(key);
	}

	public void addValue(Object key, Object value) {
		primitives.put(key, new Value(value));
		checkSize();
	}

	private void checkSize() {
		if (maxCacheSize > 0 && primitives.size() > maxCacheSize) {
			long[] scores = new long[primitives.size()];
			int index = 0;
			Iterator i = primitives.values().iterator();
			while (i.hasNext()) {
				Value v = (Value) i.next();
				scores[index++] = score(v.hits, v.lastHit);
			}
			Arrays.sort(scores);
			long threshold = scores[CACHE_PURGE_SIZE];
			i = primitives.keySet().iterator();
			while (i.hasNext()) {
				Object key = i.next();
				Value v = (Value) primitives.get(key);
				if (score(v.hits, v.lastHit) < threshold) {
					i.remove();
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