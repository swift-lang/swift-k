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
 * Created on Jan 11, 2013
 */
package k.rt;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.globus.cog.karajan.analyzer.CompilerSettings;

public class VarFutureArgChannel<T> implements Channel<T> {
	private final Frame f;
    private int index;
    private Channel<T> vargs;
    private final int endIndex, startIndex;
    private List<String> names;
    
    public VarFutureArgChannel(Frame f, int startIndex, int endIndex) {
        this.f = f;
        this.startIndex = startIndex;
        this.index = startIndex;
        this.endIndex = endIndex;
        setFutures();
    }
    
    private void setFutures() {
    	for (int i = startIndex; i <= endIndex; i++) {
    		f.set(i, new FutureObject());
    	}
	}

	public void setNames(List<String> names) {
        this.names = names;
    }

    @Override
    public synchronized boolean add(T value) {
        if (index <= endIndex) {
            if (CompilerSettings.DEBUG) {
                f.setName(index, names.get(index - startIndex));
            }
            ((FutureObject) f.get(index++)).setValue(value);
        }
        else {
            if (index++ == endIndex + 1) {
                vargs = new FutureMemoryChannel<T>();
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
        sb.append("F<");
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
