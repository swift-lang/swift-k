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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public abstract class AbstractClosedStructDataNode extends AbstractClosedDataNode {
    private DSHandle[] fields;
    
    public AbstractClosedStructDataNode(Field field) {
        super(field);
        fields = new DSHandle[field.getType().getFields().size()];
    }
    
    protected void setField(String name, DSHandle n) throws NoSuchFieldException {
        fields[field.getType().getFieldIndex(name)] = n;
    }
    
    @Override
    public synchronized DSHandle getField(Comparable<?> key) throws NoSuchFieldException {
        return fields[field.getType().getFieldIndex((String) key)];
    }
    
    @Override
    public Collection<DSHandle> getAllFields() throws InvalidPathException, HandleOpenException {
        return Arrays.asList(fields);
    }

    @Override
    public void getFringePaths(List<Path> list, Path myPath) throws HandleOpenException {
        Type t = field.getType();
        for (Field f : t.getFields()) {
            AbstractDataNode child;
            try {
                child = (AbstractDataNode) getField(f.getId());
            }
            catch (Exception e) {
                throw new RuntimeException("Structure inconsistency detected for field " + f);
            }
            Path fullPath = myPath.addLast(f.getId());
            child.getFringePaths(list, fullPath);
        }
    }
    
    @Override
    protected void getLeaves(List<DSHandle> list) throws HandleOpenException {
        Type t = field.getType();
        for (Field f : t.getFields()) {
            AbstractDataNode child;
            try {
                child = (AbstractDataNode) getField(f.getId());
            }
            catch (Exception e) {
                throw new RuntimeException("Structure inconsistency detected for field " + f);
            }
            child.getLeaves(list);
        }
    }

    
    public boolean isArray() {
        return false;
    }
}
