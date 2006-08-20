//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2006
 */
package org.globus.cog.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An implementation of <tt>Set</tt> which guarantees that iterations
 * on the set always occur on a consistent snapshot of the set. 
 * 
 */

public class CopyOnWriteHashSet implements Set {
	private Set set = Collections.EMPTY_SET;
	private int lock;	

	public int size() {
		return set.size();
	}

	public boolean isEmpty() {
		return set.isEmpty();
	}

	public boolean contains(Object o) {
		return set.contains(o);
	}
	
	public synchronized void release() {
		if (lock > 0) {
			lock--;
		}
	}

	public synchronized Iterator iterator() {
		lock++;
		return set.iterator();
	}

	public Object[] toArray() {
		return set.toArray();
	}

	public Object[] toArray(Object[] o) {
		return set.toArray(o);
	}

	public synchronized boolean add(Object o) {
		if (lock > 0 || set.isEmpty()) {
			return copyAndAdd(o);
		}
		else {
			return set.add(o);
		}
	}
	
	private boolean copyAndAdd(Object o) {
		Set newset = new HashSet(set);
		boolean b = newset.add(o);
		set = newset;
		lock = 0;
		return b;
	}

	public boolean remove(Object o) {
		if (lock > 0) {
			return copyAndRemove(o);
		}
		else {
			return set.remove(o);
		}
	}
	
	private boolean copyAndRemove(Object o) {
		Set newset = new HashSet(set);
		boolean b = newset.remove(o);
		set = newset;
		lock = 0;
		return b;
	}

	public boolean containsAll(Collection c) {
		return set.containsAll(c);
	}

	public synchronized boolean addAll(Collection c) {
		if (lock > 0 || set.isEmpty()) {
			return copyAndAddAll(c);
		}
		else {
			return set.addAll(c);
		}
	}
	
	private boolean copyAndAddAll(Collection c) {
		Set newset = new HashSet(set);
		boolean b = newset.addAll(c);
		set = newset;
		lock = 0;
		return b;
	}

	public synchronized boolean retainAll(Collection c) {
		if (lock > 0) {
			return copyAndRetainAll(c);
		}
		else {
			return set.retainAll(c);
		}
	}
	
	private boolean copyAndRetainAll(Collection c) {
		Set newset = new HashSet(set);
		boolean b = newset.retainAll(c);
		set = newset;
		lock = 0;
		return b;
	}

	public boolean removeAll(Collection c) {
		if (lock > 0) {
			return copyAndRemoveAll(c);
		}
		else {
			return set.removeAll(c);
		}
	}
	
	private boolean copyAndRemoveAll(Collection c) {
		Set newset = new HashSet(set);
		boolean b = newset.removeAll(c);
		set = newset;
		lock = 0;
		return b;
	}

	public synchronized void clear() {
		set = Collections.EMPTY_SET;
		lock = 0;
	}

	public boolean equals(Object obj) {
		if (obj instanceof CopyOnWriteHashSet) {
			return set.equals(((CopyOnWriteHashSet) obj).set);
		}
		else if (obj instanceof Set) {
			return set.equals(obj);
		}
		return false;
	}

	public int hashCode() {
		return set.hashCode();
	}

	public String toString() {
		return set.toString();
	};
	
	
}
