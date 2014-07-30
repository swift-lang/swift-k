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
 * Created on Dec 11, 2012
 */
package k.rt;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EmptyChannel<T> implements Channel<T> {

	@Override
	public Iterator<T> iterator() {
		List<T> l = Collections.emptyList();
		return l.iterator();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public T get(int index) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean add(T value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAll(Channel<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public List<T> getAll() {
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] toArray() {
		return (T[]) new Object[0];
	}

	@Override
	public T removeFirst() {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean isClosed() {
		return true;
	}

	@Override
	public void close() {
	}

	@Override
	public Channel<T> subChannel(int fromIndex) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public Channel<T> subChannel(int fromIndex, int size) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		return null;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
	}
}
