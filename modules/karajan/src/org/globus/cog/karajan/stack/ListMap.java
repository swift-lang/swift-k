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

/**
 * An implementation of {@link java.util.Map} backed by an array with sequential
 * access and a fixed size of 4 entries. It has a smaller memory consumption
 * than {@link java.util.HashMap}, but O(n) put/lookup time.
 * 
 * @author Mihael Hategan
 * 
 */
public final class ListMap implements Map {
	private Object s1, s2, s3, s4;
	private Object o1, o2, o3, o4;
	private int next;

	public ListMap() {
	}

	public void clear() {
		next = 0;
	}

	public boolean containsKey(Object key) {
		return keyIndex(key) != -1;
	}

	private int keyIndex(Object key) {
		switch (next) {
			case 4:
				if (key.equals(s4)) {
					return 3;
				}
			case 3:
				if (key.equals(s3)) {
					return 2;
				}
			case 2:
				if (key.equals(s2)) {
					return 1;
				}
			case 1:
				if (key.equals(s1)) {
					return 0;
				}
			default:
				return -1;
		}
	}

	public boolean containsValue(Object value) {
		if (value == null) {
			return o1 == null || o2 == null || o3 == null || o4 == null;
		}
		else {
			return value.equals(o1) || value.equals(o2) || value.equals(o3) || value.equals(o4);
		}
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
						switch (n++) {
							case 0:
								return new Entry(s1, o1);
							case 1:
								return new Entry(s2, o2);
							case 2:
								return new Entry(s3, o3);
							default:
								return new Entry(s4, o4);
						}
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

	public synchronized Object get(Object key) {
		int ki = keyIndex(key);
		switch (ki) {
			case 0:
				return o1;
			case 1:
				return o2;
			case 2:
				return o3;
			case 3:
				return o4;
			default:
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
						switch (n++) {
							case 0:
								return s1;
							case 1:
								return s2;
							case 2:
								return s3;
							default:
								return s4;
						}
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

	public synchronized Object put(Object key, Object value) {
		int ki = keyIndex(key);
		Object old = null;
		switch (ki) {
			case -1:
				ki = next++;
			default:
				switch (ki) {
					case 0:
						old = o1;
						s1 = key;
						o1 = value;
						break;
					case 1:
						old = o2;
						s2 = key;
						o2 = value;
						break;
					case 2:
						old = o3;
						s3 = key;
						o3 = value;
						break;
					case 3:
						old = o4;
						s4 = key;
						o4 = value;
						break;
				}
		}
		return old;
	}

	public void putAll(Map t) {
		Iterator i = t.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			put(e.getKey(), e.getValue());
		}
	}

	public synchronized Object remove(Object key) {
		int ki = keyIndex(key);
		if (ki == -1) {
			return null;
		}
		else {
			Object old = null;
			switch (ki) {
				case 0:
					old = o1;
					o1 = o2;
					o2 = o3;
					o3 = o4;
					o4 = null;
					s1 = s2;
					s2 = s3;
					s3 = s4;
					break;
				case 1:
					old = o2;
					o2 = o3;
					o3 = o4;
					o4 = null;
					s2 = s3;
					s3 = s4;
					break;
				case 2:
					old = o3;
					o3 = o4;
					o4 = null;
					s3 = s4;
					break;
				case 3:
					old = o4;
					o4 = null;
					break;
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
				switch (index) {
					case 0:
						return o1;
					case 1:
						return o2;
					case 2:
						return o3;
					default:
						return o4;
				}
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
