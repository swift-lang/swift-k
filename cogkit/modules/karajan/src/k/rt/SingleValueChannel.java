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
 * Created on Dec 17, 2012
 */
package k.rt;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SingleValueChannel<T> implements Channel<T> {
	private T value;

	@Override
	public Iterator<T> iterator() {
		return getAll().iterator();
	}

	@Override
	public int size() {
		return value == null ? 0 : 1;
	}

	@Override
	public T get(int index) {
		if (value != null && index == 0) {
			return value;
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean add(T value) {
		if (this.value == null) {
			this.value = value;
			return true;
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> values) {
		for (T o : values) {
			add(o);
		}
		return true;
	}

	@Override
	public void addAll(Channel<? extends T> c) {
		for (T o : c) {
			add(o);
		}
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}

	@Override
	public List<T> getAll() {
		List<T> l;
		if (value == null) {
			l = Collections.emptyList();
		}
		else {
			l = Collections.singletonList(value);
		}
		return l;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] toArray() {
		if (value == null) {
			return (T[]) new Object[0];
		}
		else {
			return (T[]) new Object[] { value };
		}
	}

	@Override
	public T removeFirst() {
		if (value != null) {
			T v = value;
			value = null;
			return v;
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public boolean isClosed() {
		return value != null;
	}

	@Override
	public void close() {
	}

	@Override
	public Channel<T> subChannel(int fromIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Channel<T> subChannel(int fromIndex, int size) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		return o == value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> S[] toArray(S[] a) {
		if (value == null) {
			if (a.length == 0) {
				return a;
			}
			else {
				return (S[]) new Object[0];
			}
		}
		else {
			if (a.length == 1) {
				a[0] = (S) value;
				return a;
			}
			else {
				return (S[]) new Object[] {value};
			}
		}
	}

	@Override
	public boolean remove(Object o) {
		if (o == value) {
			value = null;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (o != value) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (c.contains(value)) {
			value = null;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (!c.contains(value)) {
			value = null;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void clear() {
		value = null;
	}
}
