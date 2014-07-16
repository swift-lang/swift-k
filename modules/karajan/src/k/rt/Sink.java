//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 30, 2007
 */
package k.rt;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class Sink<T> extends AbstractChannel<T> {

	public T get(int index) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return -1;
	}

	@Override
	public boolean addAll(Collection<? extends T> values) {
		for (T v : values) {
			add(v);
		}
		return true;
	}

	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> getAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T removeFirst() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T[] toArray() {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
}
