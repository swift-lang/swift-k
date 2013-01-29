//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 2, 2005
 */
package k.rt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class OrderedParallelChannel<T> extends AbstractChannel<T> {
	private final Channel<T> dest;
	private final OrderedParallelChannel<T> prev;
	private OrderedParallelChannel<T> next;
	private List<T> buffer;
	private boolean closed, prevClosed;

	public OrderedParallelChannel(Channel<T> dest, OrderedParallelChannel<T> prev) {
		this.dest = dest;
		if (prev != null) {
			this.prev = prev;
			synchronized(prev) {
			    prev.next = this;
			    if (prev.isClosed()) {
			        prevClosed = true;
			    }
			}
			if (prevClosed) {
			    prevClosed();
			}
		}
		else {
			this.prev = null;
		}
	}

	private synchronized boolean isPrevClosed() {
		if (prev == null) {
			return true;
		}
		else {
			return prevClosed;
		}
	}

	private void initBuffer() {
		if (buffer == null) {
			buffer = new ArrayList<T>();
		}
	}

	public final boolean isClosed() {
		return closed && isPrevClosed();
	}

	public synchronized void close() {
		boolean prevClosed = isPrevClosed();

		if (prevClosed) {
			flushBuffer();
		}

		if (next != null && prevClosed) {
			next.prevClosed();
		}
		this.closed = true;
	}

	private void flushBuffer() {
		if (buffer != null) {
			dest.addAll(buffer);
			buffer = null;
		}
	}

	protected void prevClosed() {
		OrderedParallelChannel<T> i = this;
		while (i != null) {
			synchronized (i) {
				i.flushBuffer();
				i.prevClosed = true;
				if (i.closed) {
					i = i.next;
				}
				else {
					break;
				}
			}
		}
	}

	public synchronized boolean add(T value) {
		if (isPrevClosed()) {
			dest.add(value);
		}
		else {
			initBuffer();
			buffer.add(value);
		}
		return true;
	}

	public synchronized boolean addAll(Collection<? extends T> args) {
		if (isPrevClosed()) {
			dest.addAll(args);
		}
		else {
			initBuffer();
			buffer.addAll(args);
		}
		return !args.isEmpty();
	}

	public String toString() {
		if (buffer != null) {
			return "-" + buffer + "-";
		}
		else {
			return "-||-";
		}
	}

	public OrderedParallelChannel<T> getNext() {
		return next;
	}

	public T get(int index) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
    public List<T> getAll() {
        throw new UnsupportedOperationException();
    }

	
    @Override
    public T[] toArray() {
        throw new UnsupportedOperationException();
    }

    public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T removeFirst() {
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
