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
 * Created on Feb 5, 2013
 */
package org.griphyn.vdl.mapping;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import k.rt.ConditionalYield;

import org.griphyn.vdl.karajan.Pair;
import org.griphyn.vdl.mapping.nodes.ArrayHandle;

public class OpenArrayEntries implements Iterable<List<?>> {
    private List<Comparable<?>> keyList;
    private Map<Comparable<?>, DSHandle> array;
    private ArrayHandle source;

    public OpenArrayEntries(List<Comparable<?>> keyList, Map<Comparable<?>, DSHandle> array, ArrayHandle source) {
        this.keyList = keyList;
        this.array = array;
        this.source = source;
    }
    
    @Override
    public String toString() {
        return "OpenArrayEntries[" + keyList + "]";
    }

    @Override
    public Iterator<List<?>> iterator() {
        return new Iterator<List<?>>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                synchronized(source) {
                    if (index < keyList.size()) {
                        return true;
                    }
                    else {
                        if (source.isClosed()) {
                            return false;
                        }
                        else {
                            throw new ConditionalYield(source, keyList.size());
                        }
                    }
                }
            }

            @Override
            public List<?> next() {
                synchronized(source) {
                    if (index < keyList.size()) {
                        Comparable<?> key = keyList.get(index++);
                        return new Pair<Object>(key, array.get(key));
                    }
                    else {
                        if (source.isClosed()) {
                            throw new NoSuchElementException();
                        }
                        else {
                            throw new ConditionalYield(source, keyList.size());
                        }
                    }
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public String toString() {
                return OpenArrayEntries.this + "[" + index + " / " + keyList.size() + "]";
            }
        };
    }
}
