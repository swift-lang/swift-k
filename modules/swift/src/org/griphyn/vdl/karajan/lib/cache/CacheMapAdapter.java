/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jul 15, 2008
 */
package org.griphyn.vdl.karajan.lib.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CacheMapAdapter implements Map<Object, Collection<String>> {
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

	public Set<Map.Entry<Object, Collection<String>>> entrySet() {
		throw new UnsupportedOperationException();
	}

	public Collection<String> get(Object key) {
		return cache.getPaths(key);
	}

	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	public Set<Object> keySet() {
		throw new UnsupportedOperationException();
	}

	public Collection<String> put(Object key, Collection<String> value) {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map<? extends Object, ? extends Collection<String>> t) {
		throw new UnsupportedOperationException();
	}

	public Collection<String> remove(Object key) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		throw new UnsupportedOperationException();
	}

	public Collection<Collection<String>> values() {
		throw new UnsupportedOperationException();
	}
	
	public String toString() {
	    return cache.toString();
	}
}
