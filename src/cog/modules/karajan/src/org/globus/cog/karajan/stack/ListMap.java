//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 11, 2008
 */
package org.globus.cog.karajan.stack;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ListMap implements Map {
	private Map.Entry[] map;
	private int next;

	public ListMap() {
		map = new Map.Entry[4];
	}

	public void clear() {
		next = 0;
	}

	public boolean containsKey(Object key) {
		return keyIndex(key) != -1;
	}

	private int keyIndex(Object key) {
		for (int i = 0; i < next; i++) {
			if (map[i].getKey().equals(key)) {
				return i;
			}
		}
		return -1;
	}

	public boolean containsValue(Object value) {
		for (int i = 0; i < next; i++) {
			if ((value == null && map[i].getValue() == null) || map[i].getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}

	public Set entrySet() {
		return new AbstractSet() {

			public Iterator iterator() {
				return new Iterator() {
					private int n;
					
					public boolean hasNext() {
						return n < next;
					}

					public Object next() {
						return map[n++];
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

			public int size() {
				return next;
			}
			
		};
	}

	public Object get(Object key) {
		int ki = keyIndex(key);
		if (ki != -1) {
			return map[ki].getValue();
		}
		else {
			return null;
		}
	}

	public boolean isEmpty() {
		return next == 0;
	}

	public Set keySet() {
		return new AbstractSet() {

			public Iterator iterator() {
				return new Iterator() {
					private int n;
					
					public boolean hasNext() {
						return n < next;
					}

					public Object next() {
						return map[n++].getKey();
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

			public int size() {
				return next;
			}
		};
	}

	public Object put(Object key, Object value) {
		int ki = keyIndex(key);
		if (ki == -1) {
			if (next < map.length) {
				map[next] = new Entry(key, value);
				next++;
			}
			return null;
		}
		else {
			return map[ki].setValue(value);
		}
	}

	public void putAll(Map t) {
		Iterator i = t.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			put(e.getKey(), e.getValue());
		}
	}

	public Object remove(Object key) {
		int ki = keyIndex(key);
		if (ki == -1) {
			return null;
		}
		else {
			Object old = map[ki].getValue();
			for (int i = ki; i < next - 1; i++) {
				map[i] = map[i + 1];
			}
			next--;
			return old;
		}
	}

	public int size() {
		return next;
	}

	public Collection values() {
		return new AbstractList() {
			public Object get(int index) {
				return map[index].getValue();
			}

			public int size() {
				return next;
			}
		};
	}

	private static class Entry implements Map.Entry {
		private final Object key;
		private Object value;
		
		public Entry(Object key, Object value) {
			this.key = key;
			this.value = value;
		}

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public Object setValue(Object value) {
			Object old = this.value;
			this.value = value;
			return old;
		}
	}
}
