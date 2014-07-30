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
 * A copy of the set is only performed if the set is locked for iteration. 
 * The set is automatically locked for iteration by {@link #iterator()} and
 * must be unlocked when the iteration is done using {@link #release()}:
 * <pre>
 *  Iterator<T> i = set.iterator();
 *  try {
 *      while (i.hasNext()) {
 *          ...
 *      }
 *  }
 *  finally {
 *      set.release();
 *  }
 * </pre>
 * 
 * Once a copy is made, subsequent mutative operations will not trigger
 * another copy unless a new iterator is created from this set.
 * 
 */

public class CopyOnWriteHashSet<T> implements Set<T>, Cloneable {
	private Set<T> set = Collections.emptySet();
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
	
	public synchronized void release(Iterator<T> it) {
	    if (((LIterator) it).set == set) {
    		if (lock > 0) {
    			lock--;
    		}
	    }
	}

	public synchronized Iterator<T> iterator() {
		lock++;
		return new LIterator(set);
	}

	public Object[] toArray() {
		return set.toArray();
	}

    public <S> S[] toArray(S[] a) {
        return set.toArray(a);
    }

    public synchronized boolean add(T o) {
		if (lock > 0 || set.isEmpty()) {
			return copyAndAdd(o);
		}
		else {
			return set.add(o);
		}
	}
	
	private boolean copyAndAdd(T o) {
		Set<T> newset = new HashSet<T>(set);
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
		Set<T> newset = new HashSet<T>(set);
		boolean b = newset.remove(o);
		set = newset;
		lock = 0;
		return b;
	}

	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	public synchronized boolean addAll(Collection<? extends T> c) {
		if (lock > 0 || set.isEmpty()) {
			return copyAndAddAll(c);
		}
		else {
			return set.addAll(c);
		}
	}
	
	private boolean copyAndAddAll(Collection<? extends T> c) {
		Set<T> newset = new HashSet<T>(set);
		boolean b = newset.addAll(c);
		set = newset;
		lock = 0;
		return b;
	}

	public synchronized boolean retainAll(Collection<?> c) {
		if (lock > 0) {
			return copyAndRetainAll(c);
		}
		else {
			return set.retainAll(c);
		}
	}
	
	private boolean copyAndRetainAll(Collection<?> c) {
		Set<T> newset = new HashSet<T>(set);
		boolean b = newset.retainAll(c);
		set = newset;
		lock = 0;
		return b;
	}

	public boolean removeAll(Collection<?> c) {
		if (lock > 0) {
			return copyAndRemoveAll(c);
		}
		else {
			return set.removeAll(c);
		}
	}
	
	private boolean copyAndRemoveAll(Collection<?> c) {
		Set<T> newset = new HashSet<T>(set);
		boolean b = newset.removeAll(c);
		set = newset;
		lock = 0;
		return b;
	}

	public synchronized void clear() {
		set = Collections.emptySet();
		lock = 0;
	}

	public boolean equals(Object obj) {
		if (obj instanceof CopyOnWriteHashSet<?>) {
			return set.equals(((CopyOnWriteHashSet<?>) obj).set);
		}
		else if (obj instanceof Set<?>) {
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
	
	public Object clone() {
	    Set<T> tmp = new HashSet<T>(size()*2);
	    
	    Iterator<T> it = iterator();
	    while (it.hasNext()) {
	        T o = it.next();
	        tmp.add(o);
	    }
	    release(it);
	    
	    CopyOnWriteHashSet<T> result = new CopyOnWriteHashSet<T>();
	    result.lock = 0;
	    result.set = tmp;
	    return result;
	}
	
	private class LIterator implements Iterator<T> {
	    private Iterator<T> it;
	    public Set<T> set;
	    
	    public LIterator(Set<T> set) {
	        this.set = set;
	        this.it = set.iterator();
	    }

        public boolean hasNext() {
            return it.hasNext();
        }

        public T next() {
            return it.next();
        }

        public void remove() {
            it.remove();
        }
	}
}
