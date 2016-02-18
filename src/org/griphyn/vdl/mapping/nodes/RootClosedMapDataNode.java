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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import k.thr.LWThread;

import org.griphyn.vdl.karajan.Pair;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.mapping.file.FileGarbageCollector;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Types;

public class RootClosedMapDataNode extends AbstractClosedDataNode implements ArrayHandle, RootHandle {
	private int line = -1;
    private LWThread thread;
    private Mapper mapper;
    private Map<Comparable<?>, DSHandle> values;
    
    public RootClosedMapDataNode(Field field, Map<?, ?> values, DuplicateMappingChecker dmChecker) {
    	super(field);
    	setValues(values);
    	if (getType().itemType().hasMappedComponents()) {
            this.mapper = new InitMapper(dmChecker);
        }
    }
    
    private void setValues(Map<?, ?> m) {
        values = new HashMap<Comparable<?>, DSHandle>();
        for (Map.Entry<?, ? extends Object> e : m.entrySet()) {
            Comparable<?> k = (Comparable<?>) e.getKey();
            Object n = e.getValue();
            if (n instanceof DSHandle) {
                values.put(k, (DSHandle) n);
            }
            else if (n instanceof String) {
                values.put(k, 
                    NodeFactory.newNode(Field.Factory.createField(k, Types.STRING), getRoot(), this, n));
            }
            else if (n instanceof Integer) {
                values.put(k, 
                    NodeFactory.newNode(Field.Factory.createField(k, Types.INT), getRoot(), this, n));
            }
            else if (n instanceof Double) {
                values.put(k, 
                    NodeFactory.newNode(Field.Factory.createField(k, Types.FLOAT), getRoot(), this, n));
            }
            else {
                throw new RuntimeException(
                        "An array variable can only be initialized by a list of DSHandle or primitive values");
            }
        }
    }

    @Override
    public RootHandle getRoot() {
        return this;
    }

    @Override
    public DSHandle getParent() {
        return null;
    }

    @Override
    public Path getPathFromRoot() {
        return Path.EMPTY_PATH;
    }

    @Override
    public void init(Mapper mapper) {
        if (!getType().itemType().hasMappedComponents()) {
            return;
        }
        if (mapper == null) {
            initialized();
        }
        else {
            this.getInitMapper().setMapper(mapper);
            this.mapper.initialize(this);
        }  
    }
    
    @Override
    public void mapperInitialized(Mapper mapper) {
        synchronized(this) {
            this.mapper = mapper;
        }
        initialized();
    }

    protected void initialized() {
        if (variableTracer.isEnabled()) {
            variableTracer.trace(thread, line, getName() + " INITIALIZED " + mapper);
        }
    }
    
    public synchronized Mapper getMapper() {
        if (mapper instanceof InitMapper) {
            return ((InitMapper) mapper).getMapper();
        }
        else {
            return mapper;
        }
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public void setLine(int line) {
        this.line = line;
    }

    @Override
    public void setThread(LWThread thread) {
        this.thread = thread;
    }

    @Override
    public LWThread getThread() {
        return thread;
    }
    
    @Override
    public String getName() {
        return (String) getField().getId();
    }
    
    @Override
    protected AbstractDataNode getParentNode() {
        return null;
    }

    @Override
    public Mapper getActualMapper() {
        return mapper;
    }

    @Override
    public void closeArraySizes() {
        // already closed
    }

    @Override
    public Object getValue() {
        return values;
    }
    
    @Override
    public Map<Comparable<?>, DSHandle> getArrayValue() {
        return values;
    }

    @Override
    public boolean isArray() {
        return true;
    }
    
    

    @Override
    public Iterable<List<?>> entryList() {
        final Iterator<Map.Entry<Comparable<?>, DSHandle>> i = values.entrySet().iterator();
        return new Iterable<List<?>>() {
            @Override
            public Iterator<List<?>> iterator() {
                return new Iterator<List<?>>() {
                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public List<?> next() {
                        Map.Entry<Comparable<?>, DSHandle> e = i.next();
                        return new Pair<Object>(e.getKey(), e.getValue());
                    }

                    @Override
                    public void remove() {
                        i.remove();
                    }
                };
            }
        };
    }

    @Override
    protected Object getRawValue() {
        return values;
    }

    @Override
    protected void getFringePaths(List<Path> list, Path myPath)
            throws HandleOpenException {
        for (Map.Entry<Comparable<?>, DSHandle> e : values.entrySet()) {
            DSHandle h = e.getValue();
            if (h instanceof AbstractDataNode) {
                AbstractDataNode ad = (AbstractDataNode) h;
                ad.getFringePaths(list, myPath.addLast(e.getKey()));
            }
            else {
                list.addAll(h.getFringePaths());
            }
        }
    }

    @Override
    public void getLeaves(List<DSHandle> list) throws HandleOpenException {
        for (Map.Entry<Comparable<?>, DSHandle> e : values.entrySet()) {
            DSHandle h = e.getValue();
            if (h instanceof AbstractDataNode) {
                AbstractDataNode ad = (AbstractDataNode) h;
                ad.getLeaves(list);
            }
            else {
                list.addAll(h.getLeaves());
            }
        }
    }
    
    @Override
    public int arraySize() {
        return values.size();
    }
    
    @Override
    protected void clean0() {
        FileGarbageCollector.getDefault().clean(this);
        super.clean0();
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (!isCleaned()) {
            clean();
        }
        super.finalize();
    }
}
