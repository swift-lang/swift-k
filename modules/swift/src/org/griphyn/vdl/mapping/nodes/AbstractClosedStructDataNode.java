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
