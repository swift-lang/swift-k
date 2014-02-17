//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 6, 2012
 */
package k.rt;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.globus.cog.karajan.analyzer.CompilerSettings;

public class VarArgChannel<T> implements Channel<T> {
	private final Frame f;
	private int index;
	private Channel<T> vargs;
	private final int endIndex, startIndex;
	private List<String> names;
	
	public VarArgChannel(Frame f, int startIndex, int endIndex) {
		this.f = f;
		this.startIndex = startIndex;
		this.index = startIndex;
		this.endIndex = endIndex;
	}
	
	public void setNames(List<String> names) {
		this.names = names;
	}

	@Override
	public synchronized boolean add(T value) {
		if (index <= endIndex) {
			if (CompilerSettings.DEBUG) {
				if (names != null) {
					f.setName(index, names.get(index - startIndex));
				}
			}
			f.set(index++, value);
		}
		else {
			if (index++ == endIndex + 1) {
				vargs = new MemoryChannel<T>();
			}
			vargs.add(value);
		}
		return true;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> values) {
		for (T o : values) {
			add(o);
		}
		return true;
	}

	@Override
	public synchronized void addAll(Channel<? extends T> c) {
		for (T o : c) {
            add(o);
        }
	}

	@Override
	public boolean isEmpty() {
		return vargs == null;
	}

	@Override
	public List<T> getAll() {
		if (vargs == null) {
			return Collections.emptyList();
		}
		else {
			return vargs.getAll();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] toArray() {
		if (vargs == null) {
			return (T[]) new Object[0];
		}
		else {
			return (T[]) vargs.toArray();
		}
	}

	@Override
	public T removeFirst() {
		if (vargs != null) {
			return vargs.removeFirst();
		}
		else {
			throw new NoSuchElementException();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		if (vargs == null) {
			return ((List<T>) Collections.emptyList()).iterator();
		}
		else {
			return vargs.iterator();
		}
	}

	@Override
	public int size() {
		if (vargs == null) {
			return 0;
		}
		else {
			return vargs.size();
		}
	}
	
	public int argSize() {
		return index - startIndex;
	}

	@Override
	public T get(int index) {
		if (vargs == null) {
			throw new NoSuchElementException();
		}
		else {
			return vargs.get(index);
		}
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
		if (vargs == null) {
			throw new IndexOutOfBoundsException(String.valueOf(fromIndex));
		}
		else {
			return vargs.subChannel(fromIndex);
		}
	}

	@Override
	public Channel<T> subChannel(int fromIndex, int size) {
		if (vargs == null) {
			throw new IndexOutOfBoundsException(String.valueOf(fromIndex));
		}
		else {
			return vargs.subChannel(fromIndex, size);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		for (int i = startIndex; i <= endIndex; i++) {
			if (i == index) {
				sb.append("*");
			}
			sb.append(f.get(i));
			if (i != endIndex) {
				sb.append(", ");
			}
		}
		if (vargs == null) {
			sb.append("[]");
		}
		else {
			sb.append(vargs.toString());
		}
		sb.append(">");
		return sb.toString();
	}

	@Override
	public boolean contains(Object o) {
		if (vargs != null) {
			return vargs.contains(o);
		}
		else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> S[] toArray(S[] a) {
		if (vargs != null) {
			return vargs.toArray(a);
		}
		else {
			return (S[]) new Object[0];
		}
	}

	@Override
	public boolean remove(Object o) {
		if (vargs != null) {
			return vargs.remove(o);
		}
		else {
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (vargs != null) {
			return vargs.containsAll(c);
		}
		else {
			return false;
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (vargs != null) {
			return vargs.removeAll(c);
		}
		else {
			return false;
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (vargs != null) {
			return vargs.retainAll(c);
		}
		else {
			return false;
		}
	}

	@Override
	public void clear() {
		vargs = null;
	}
}
