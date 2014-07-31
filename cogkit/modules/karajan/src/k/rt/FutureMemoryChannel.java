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
 * Created on Mar 19, 2009
 */
package k.rt;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;



public class FutureMemoryChannel<T> extends MemoryChannel<T> implements FutureValue {
    private boolean closed;
    private RuntimeException exception;
    private Collection<FutureListener> listeners;
    
    public FutureMemoryChannel() {
        super();
    }

    public FutureMemoryChannel(Collection<T> all) {
        super(all);
    }

    @Override
    public synchronized boolean add(T value) {
        boolean r = super.add(value);
        notifyListeners();
        return r;
    }

    @Override
    public synchronized boolean addAll(Collection<? extends T> values) {
        boolean b = super.addAll(values);
        notifyListeners();
        return b;
    }
    
    public synchronized void fail(RuntimeException e) {
        this.exception = e;
        notifyListeners();
    }

    @Override
    public synchronized T get(int index) {
        if (exception != null) {
            throw exception;
        }
        if (super.size() > index || closed) {
            return super.get(index);
        }
        else {
            throw new ConditionalYield(this, super.size());
        }
    }
    
    public synchronized int available() {
    	return super.size();
    }

    @Override
	public Object getValue() {
		size();
		return this;
	}

	@Override
    public synchronized List<T> getAll() {
        if (exception != null) {
            throw exception;
        }
        if (closed) {
            return super.getAll();
        }
        else {
            throw new ConditionalYield(this, super.size());
        }
    }

    @Override
    public synchronized Iterator<T> iterator() {
        if (exception != null) {
            throw exception;
        }
        if (closed) {
            return super.iterator();
        }
        else {
            return new FutureIterator<T>();
        }
    }
    
    @Override
    public int size() {
        if (exception != null) {
            throw exception;
        }
        if (closed) {
            return super.size();
        }
        else {
            throw new ConditionalYield(this, super.size());
        }
    }

    @Override
    public synchronized void addListener(FutureListener l, ConditionalYield y) {
        if (listeners == null) {
            listeners = new LinkedList<FutureListener>();
        }
        listeners.add(l);
        if (closed || (y != null && (y.getSequence() != super.size()))) {
            notifyListeners();
        }
    }

    @Override
    public synchronized void close() {
        this.closed = true;
        notifyListeners();
    }
    
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        Iterator<?> i = super.getAll().iterator();
        while (i.hasNext()) {
            sb.append(i.next());
            if (i.hasNext() || !closed) {
                sb.append(", ");
            }
        }
        if (!closed) {
            sb.append("...");
        }
        sb.append(']');
        return sb.toString();
    }

    private void notifyListeners() {
        if (listeners != null) {
            Iterator<FutureListener> i = listeners.iterator();
            while (i.hasNext()) {
                i.next().futureUpdated(this);
                i.remove();
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
    
    private class FutureIterator<S> implements Iterator<S> {
        @Override
        public boolean hasNext() {
            synchronized(FutureMemoryChannel.this) {
                if (exception != null) {
                	throw exception;
                }
                if (closed) {
                    if (size() > 0) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else {
                    get(0);
                    return true;
                }
            }
        }

        @Override
        public S next() {
            synchronized(FutureMemoryChannel.this) {
                if (exception != null) {
                    throw exception;
                }
                @SuppressWarnings("unchecked")
				S o = (S) get(0);
                removeFirst();
                return o;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
