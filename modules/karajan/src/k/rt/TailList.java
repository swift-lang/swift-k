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
 * Created on Aug 3, 2009
 */
package k.rt;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class TailList<E> implements List<E> {
    
    private final List<E> l;
    private final int fromIndex;
    
    public TailList(List<E> l, int fromIndex) {
        this.l = l;
        this.fromIndex = fromIndex;
    }
    
    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean contains(Object o) {
        return l.indexOf(o) >= fromIndex;
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public E get(int index) {
        return l.get(fromIndex + index);
    }
    
    @Override
    public int indexOf(Object o) {
        int i = l.indexOf(o) - fromIndex;
        return i >= 0 ? i : -1;
    }
    
    @Override
    public boolean isEmpty() {
        return l.size() <= fromIndex;
    }
    
    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E remove(int index) {
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
    public E set(int index, E element) {
        return l.set(index + fromIndex, element);
    }
    
    @Override
    public int size() {
        return l.size() - fromIndex;
    }
    
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Object[] toArray() {
        return l.subList(fromIndex, l.size()).toArray();
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        return l.subList(fromIndex, l.size()).toArray(a);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = fromIndex; i < l.size(); i++) {
            sb.append(l.get(i));
            if (i < l.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }
}
