/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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