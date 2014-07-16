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
import java.util.List;

import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public abstract class AbstractFutureStructDataNode extends AbstractFutureDataNode {
    private DSHandle[] fields;
    private RuntimeException exception;
    
    public AbstractFutureStructDataNode(Field field, RootHandle root) {
        super(field);
        fields = new DSHandle[field.getType().getFields().size()];
    }
    
    @Override
    public void initialize() {
        super.initialize();
        createFieldNodes();
    }

    private void createFieldNodes() {
        for (Field f : field.getType().getFields()) {
            try {
                setField((String) f.getId(), NodeFactory.newNode(f, getRoot(), this));
            }
            catch (NoSuchFieldException e) {
                throw new RuntimeException("Type inconsistency detected for field " + f);
            }
        }
    }

    protected void setField(String name, DSHandle n) throws NoSuchFieldException {
        fields[field.getType().getFieldIndex(name)] = n;
    }
        
    @Override
    protected Object getRawValue() {
        return null;
    }
    
    @Override
    public Object getValue() {
        return Arrays.asList(fields);
    }
    
    protected void checkDataException() {
        if (exception instanceof DependentException) {
            throw (DependentException) exception;
        }
    }
    
    protected void checkMappingException() {
        if (exception instanceof MappingDependentException) {
            throw (MappingDependentException) exception;
        }
    }

    @Override
    public synchronized DSHandle getField(Comparable<?> key) throws NoSuchFieldException {
        return fields[field.getType().getFieldIndex((String) key)];
    }
    
    @Override
    public void closeDeep() {
        closeShallow();
        for (DSHandle handle : fields) {
            handle.closeDeep();
        }
    }
    
    @Override
    public void getFringePaths(List<Path> list, Path myPath) throws HandleOpenException {
        checkMappingException();
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
        checkMappingException();
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
    
    @Override
    protected void clean0() {
        for (DSHandle h : fields) {
            ((AbstractDataNode) h).clean0();
        }
        fields = null;
        exception = null;
        super.clean0();
    }
    
    @Override
    protected void checkNoValue() {
        if (exception != null) {
            throw exception;
        }
    }
    
    @Override
    public void closeArraySizes() {
        for (DSHandle h : fields) {
            h.closeArraySizes();
        }
    }
}
