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
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Field;

public class RootClosedMappedSingleDataNode extends AbstractClosedMappedSingleDataNode implements RootHandle {
	private int line = -1;
    private LWThread thread;
    private Mapper mapper;
    
    public RootClosedMappedSingleDataNode(Field field, Object value, DuplicateMappingChecker dmc) {
    	super(field, value);
    	this.mapper = new InitMapper(dmc);
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
        // does not apply
    }
}
