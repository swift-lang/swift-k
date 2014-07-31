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
 * Created on Dec 30, 2007
 */
package k.rt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ReadOnlyChannel<T> extends AbstractChannel<T> {
    private List<T> values;
		
	public ReadOnlyChannel(List<T> values) {
	    this.values = values;
	}
	
	@Override
	public int size() {
		return values.size();
	}

	@Override
	public T get(int index) {
		return values.get(index);
	}

	@Override
	public boolean add(T value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		return values.toString();
	}
	
	@Override
	public Iterator<T> iterator() {
		return values.iterator();
	}

	@Override
	public boolean addAll(Collection<? extends T> values) {
		throw new UnsupportedOperationException();
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
        return values.isEmpty();
    }

    @Override
    public T removeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Channel<T> subChannel(int fromIndex) {
        return new ReadOnlyChannel<T>(values.subList(fromIndex, values.size() - 1));
    }

    @Override
    public Channel<T> subChannel(int fromIndex, int size) {
        return new ReadOnlyChannel<T>(values.subList(fromIndex, fromIndex + size));
    }

	@Override
	public boolean contains(Object o) {
		return values.contains(o);
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return values.containsAll(c);
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
