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
 * Created on Mar 28, 2014
 */
package org.griphyn.vdl.mapping.nodes;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.griphyn.vdl.mapping.ClosedArrayEntries;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Types;


public abstract class AbstractClosedArrayDataNode extends AbstractClosedDataNode implements ArrayHandle {
    private DSHandle[] values;

    public AbstractClosedArrayDataNode(Field field, List<?> values) {
    	super(field);
    	setValues(values);
    }
    
    private void setValues(List<?> values) {
        int sz = values.size();
        this.values = new DSHandle[sz];
    	int index = 0;
        Iterator<?> i = values.iterator();
        while (i.hasNext()) {
            Object n = i.next();
            if (n instanceof DSHandle) {
                this.values[index] = (DSHandle) n;
            }
            else if (n instanceof String) {
            	this.values[index] = NodeFactory.newNode(Field.Factory.createField(index, Types.STRING), getRoot(), this, n);
            }
            else {
                throw new RuntimeException(
                        "An array variable can only be initialized by a list of DSHandle values");
            }
            index++;
        }
    }
    
    @Override
    protected Object getRawValue() {
        return null;
    }
    
    @Override
    public synchronized DSHandle getField(Comparable<?> key) throws NoSuchFieldException {
        int index = ((Integer) key).intValue();
        if (index < 0 || index >= values.length) {
            throw new NoSuchFieldException(String.valueOf(index));
        }
        return values[index];
    }

    @Override
    public Object getValue() {
        return getArrayValue();
    }
    
    @Override
    public Map<Comparable<?>, DSHandle> getArrayValue() {
        return new AbstractMap<Comparable<?>, DSHandle>() {
            @Override
            public Set<Map.Entry<Comparable<?>, DSHandle>> entrySet() {
                return new AbstractSet<Map.Entry<Comparable<?>, DSHandle>>() {
                    @Override
                    public Iterator<Map.Entry<Comparable<?>, DSHandle>> iterator() {
                        return new Iterator<Map.Entry<Comparable<?>, DSHandle>>() {
                            private int index = 0;
                            
                            @Override
                            public boolean hasNext() {
                                return index < values.length;
                            }

                            @Override
                            public Map.Entry<Comparable<?>, DSHandle> next() {
                                final int i = index;
                                index++;
                                return new Map.Entry<Comparable<?>, DSHandle>() {
                                    @Override
                                    public Comparable<?> getKey() {
                                        return Integer.valueOf(i);
                                    }

                                    @Override
                                    public DSHandle getValue() {
                                        return values[i];
                                    }

                                    @Override
                                    public DSHandle setValue(DSHandle value) {
                                        throw new UnsupportedOperationException();
                                    }
                                };
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return values.length;
                    }
                };
            }
        };
    }
    
    @Override
    public Iterable<List<?>> entryList() {
        return new ClosedArrayEntries(getArrayValue());
    }
    
    @Override
    public void getFringePaths(List<Path> list, Path myPath) throws HandleOpenException {
        for (int i = 0; i < values.length; i++) {
            Path fullPath = myPath.addLast(i, true);
            ((AbstractDataNode) values[i]).getFringePaths(list, fullPath);
        }
    }
    
    @Override
    protected void getLeaves(List<DSHandle> list) throws HandleOpenException {
        for (DSHandle h : values) {
            AbstractDataNode child = (AbstractDataNode) h;
            child.getLeaves(list);
        }
    }
    
    public boolean isArray() {
        return true;
    }

    @Override
    public int arraySize() {
        return values.length;
    }

    @Override
    public void closeArraySizes() {
        for (DSHandle h : values) {
            h.closeArraySizes();
        }
    }
}
