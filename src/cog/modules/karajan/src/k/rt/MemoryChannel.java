//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 30, 2007
 */
package k.rt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MemoryChannel<T> extends AbstractChannel<T> {
	private List<T> values;
	
	public MemoryChannel() {
	}
	
	public MemoryChannel(Collection<T> values) {
	    this.values = new ArrayList<T>(values);
	}
	
	private MemoryChannel(List<T> values) {
	    this.values = values;
	}

	public int size() {
		if (values == null) {
			return 0;
		}
		return values.size();
	}

	public T get(int index) {
		if (values == null) {
            throw new IndexOutOfBoundsException();
        }
		return values.get(index);
	}

	public boolean add(T value) {
		if (values == null) {
			values = new ArrayList<T>(4);
		}
		return values.add(value);
	}
	
	public String toString() {
		if (values == null) {
			return "MC" + System.identityHashCode(this) + "[]";
		}
		return "MC" + System.identityHashCode(this) + values.toString();
	}
	
	public Iterator<T> iterator() {
		if (values == null) {
			List<T> l = Collections.emptyList();
			return l.iterator();
		}
		return values.iterator();
	}

	public boolean addAll(Collection<? extends T> values) {
		if (this.values == null) {
			this.values = new ArrayList<T>(values);
			return true;
		}
		else {
			return this.values.addAll(values);
		}
	}

    @Override
    public List<T> getAll() {
        if (values == null) {
            return Collections.emptyList();
        }
        else {
            return values;
        }
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public T[] toArray() {
    	if (values == null) {
    		return (T[]) Collections.emptyList().toArray();
    	}
        return (T[]) values.toArray();
    }

    public ArrayList<?> asArrayList() {
        if (values == null) {
            return new ArrayList<T>();
        }
        else {
            return (ArrayList<T>) values;
        }
    }

    @Override
    public boolean isEmpty() {
    	if (values == null) {
    		return true;
    	}
        return values.isEmpty();
    }

    @Override
    public T removeFirst() {
    	if (values == null) {
    		throw new IndexOutOfBoundsException();
    	}
        return values.remove(0);
    }

    @Override
    public Channel<T> subChannel(int fromIndex) {
    	if (values == null) {
            throw new IndexOutOfBoundsException();
        }
        return new MemoryChannel<T>(new TailList<T>(values, fromIndex));
    }

    @Override
    public Channel<T> subChannel(int fromIndex, int size) {
    	if (values == null) {
            throw new IndexOutOfBoundsException();
        }
        return new MemoryChannel<T>(values.subList(fromIndex, fromIndex + size));
    }

	@Override
	public boolean contains(Object o) {
		if (values == null) {
			return false;
		}
		return values.contains(o);
	}

	@Override
	public boolean remove(Object o) {
		if (values == null) {
			return false;
		}
		return values.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (values == null) {
            return false;
        }
		return values.containsAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (values == null) {
            return false;
        }
		return values.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (values == null) {
            return false;
        }
		return values.retainAll(c);
	}

	@Override
	public void clear() {
		values = null;
	}
}
