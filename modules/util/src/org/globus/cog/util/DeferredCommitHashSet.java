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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 3, 2005
 */
package org.globus.cog.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * A synchronized implementation of <tt>Set</tt> which allows deferred modifications.
 * The set can be locked/unlocked using the <tt>lock</tt> and <tt>unlock</tt> methods. If
 * any of the <tt>add</tt>, <tt>remove</tt>, <tt>addAll</tt>, or <tt>removeAll</tt>
 * methods are called while the set is locked, their effect is deferred until the
 * set is unlocked. In other words, if an iteration is performed while the set is
 * locked, modification of the set by either the thread performing the iteration
 * or another thread, will not cause the iterator to throw a 
 * <tt>ConcurrentModificationException</tt>. Instead, the modifications of the set
 * will only take effect when the set is unlocked.
 * <br>
 * Read operations, including <tt>size</tt>, <tt>contains</tt>, <tt>equals</tt>,
 * <tt>hashSet</tt>, will not change while the set is locked. This behaviour breaks
 * the general semantics of a set. For example, the following method will return 
 * <tt>false</tt>:
 * <tt>
 * 
 * public boolean foo() {
 *     set.lock();
 *     try {
 *         set.add("a");
 *         return set.contains("a");
 *     }
 *     finally {
 *         set.unlock();
 *     }
 * }
 * 
 * </tt>
 * <br><br>
 * The implementation also uses lazy object instantiation, making it suitable for 
 * cases where the set is likely to remain empty over its lifetime.
 * <br><br>
 * Access to the set is internally synchronized.
 *
 * @deprecated The scheme used in the implementation of this class
 * can cause the commits to never or rarely occur if the set is locked
 * from multiple threads but never or ralely unlocked simultaneousely
 */
public class DeferredCommitHashSet implements Set {
	private Set set;
	private Set add, remove;
	private int lock;

	public synchronized int size() {
		return set == null ? 0 : set.size();
	}

	public synchronized boolean isEmpty() {
		return set == null || set.isEmpty();
	}

	public synchronized boolean contains(Object o) {
		return set == null ? false : set.contains(o);
	}

	public synchronized Iterator iterator() {
		return set == null ? Collections.EMPTY_SET.iterator() : set.iterator();
	}

	public synchronized Object[] toArray() {
		return set == null ? Collections.EMPTY_SET.toArray() : set.toArray();
	}

	public synchronized Object[] toArray(Object[] type) {
		return set == null ? Collections.EMPTY_SET.toArray(type) : set.toArray(type);
	}

	public synchronized boolean add(Object o) {
		if (lock > 0) {
			if (set != null && set.contains(o)) {
				return false;
			}
			return delayedAdd(o);
		}
		else {
			if (set == null) {
				set = new HashSet();
			}
			return set.add(o);
		}
	}

	private boolean delayedAdd(Object o) {
		if (add == null) {
			add = new HashSet();
		}
		if (remove != null) {
			remove.remove(o);
		}
		return add.add(o);
	}

	private boolean delayedRemove(Object o) {
		if (remove == null) {
			remove = new HashSet();
		}
		if (add != null) {
			add.remove(o);
		}
		return remove.add(o);
	}

	public synchronized boolean remove(Object o) {
		if (lock > 0) {
			if (set == null || !set.contains(o)) {
				return false;
			}
			return delayedRemove(o);
		}
		else {
			if (set == null) {
				return false;
			}
			else {
				return set.remove(o);
			}
		}
	}

	public synchronized boolean containsAll(Collection c) {
		return set != null && set.containsAll(c);
	}

	public synchronized boolean addAll(Collection c) {
		if (lock > 0) {
			if (set != null) {
				return false;
			}
			boolean any = false;
			Iterator i = c.iterator();
			while (i.hasNext()) {
				Object o = i.next();
				if (!set.contains(o)) {
					any |= delayedAdd(o);
				}
			}
			return any;
		}
		else {
			if (set == null) {
				set = new HashSet();
			}
			return set.addAll(c);
		}
	}

	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException(
				"DeferredCommitHashSet does not support retainAll()");
	}
	
	public synchronized boolean removeAll(Collection c) {
		if (lock > 0) {
			if (set != null) {
				return false;
			}
			boolean any = false;
			Iterator i = c.iterator();
			while (i.hasNext()) {
				Object o = i.next();
				if (!set.contains(o)) {
					any |= delayedRemove(o);
				}
			}
			return any;
		}
		else {
			if (set == null) {
				return false;
			}
			else {
				return set.removeAll(c);
			}
		}
	}

	public synchronized void clear() {
		if (lock > 0) {
			if (set == null) {
				return;
			}
			if (remove == null) {
				remove = new HashSet();
			}
			remove.addAll(set);
		}
		else {
			if (set != null) {
				set.clear();
			}
		}
	}

	public synchronized void lock() {
		lock++;
	}
	
	public synchronized void unlock() {
		lock--;
		if (lock == 0) {
			if (add != null) {
				if (set == null) {
					set = new HashSet();
				}
				set.addAll(add);
				add = null;
			}
			if (remove != null) {
				if (set == null) {
					return;
				}
				set.removeAll(remove);
				remove = null;
			}
		}
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof DeferredCommitHashSet) {
			DeferredCommitHashSet dchs = (DeferredCommitHashSet) obj;
			synchronized(this) {
				synchronized(dchs) {
					if (isEmpty()) {
						return dchs.isEmpty();
					}
					else {
						return set.equals(dchs.set);
					}
				}
			}
		}
		return false;
	}

	public synchronized int hashCode() {
		if (set == null) {
			return 0;
		}
		else {
			return set.hashCode();
		}
	}

	public synchronized String toString() {
		if (set == null) {
			return "[]";
		}
		else {
			return set.toString();
		}
	}
}
