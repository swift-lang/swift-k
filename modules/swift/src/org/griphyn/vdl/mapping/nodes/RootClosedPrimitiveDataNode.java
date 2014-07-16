//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2014
 */
package org.griphyn.vdl.mapping.nodes;

import k.thr.LWThread;

import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Field;

public class RootClosedPrimitiveDataNode extends AbstractClosedPrimitiveDataNode implements RootHandle {
	private int line = -1;
    private LWThread thread;
    
    public RootClosedPrimitiveDataNode(Field field, Object value) {
    	super(field, value);
    }
    
    public RootClosedPrimitiveDataNode(Field field, DependentException e) {
        super(field, e);
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
    }

    @Override
    public void mapperInitialized(Mapper mapper) {
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
    public boolean isInput() {
        // maybe not strictly true, but the only place where this matters
        // is when mapping input data, and there is no mapping for this
        // node
        return false;
    }

    @Override
    public void setInput(boolean input) {
        // noop
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
        return null;
    }
    
    @Override
    public Mapper getMapper() {
        throw new UnsupportedOperationException("Primitive types do not have a mapper");
    }
    
    @Override
    public void closeArraySizes() {
        // does not apply
    }
}
