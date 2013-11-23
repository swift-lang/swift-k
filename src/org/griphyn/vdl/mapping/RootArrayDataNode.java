/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.mapping;

import k.rt.Future;
import k.rt.FutureListener;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.lib.Tracer;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class RootArrayDataNode extends ArrayDataNode implements FutureListener, RootHandle {

    Logger logger = Logger.getLogger(RootArrayDataNode.class);
    
	private boolean initialized = false;
	private Mapper mapper;
	private AbstractDataNode waitingMapperParam;
	private DuplicateMappingChecker dmc;
	
    // previously in mapper params
    private int line = -1;
    private LWThread thread;
    private boolean input;

	
	public RootArrayDataNode(Type type) {
	    this("?", type);
	}
	/**
	 * Instantiate a root array data node with specified type.
	 */
	public RootArrayDataNode(String name, Type type) {
		super(Field.Factory.getImmutableField(name, type), null, null);
	}
	
	public RootArrayDataNode(String name, Type type, DuplicateMappingChecker dmc) {
	    this(name, type);
	    this.dmc = dmc;
	}
	
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public void setThread(LWThread thread) {
        this.thread = thread;
    }

    public LWThread getThread() {
        return thread;
    }
	
	public String getName() {
        return (String) getField().getId();
    }
	
	@Override
    public void setName(String name) {
        getField().setId(name);
    }

	@Override
	public void init(Mapper mapper) {
		this.mapper = mapper;
		if (this.mapper == null) {
			initialized();
			if (isInput()) {
                // Empty array. Clearly only in test cases.
                closeDeep();
            }
		}
		else {
			innerInit();
		}
	}

	private synchronized void innerInit() {
		if (logger.isDebugEnabled()) {
		    logger.debug("innerInit: " + this);
		}
	    
		waitingMapperParam = mapper.getFirstOpenParameter();
        if (waitingMapperParam != null) {
            waitingMapperParam.addListener(this);
            if (variableTracer.isEnabled()) {
                variableTracer.trace(getThread(), getLine(), getName() + " WAIT " 
                    + Tracer.getVarName(waitingMapperParam));
            }
            return;
        }
	    
        mapper.initialize(this);
		initialized();
		checkInputs();
		if (isClosed()) {
		    notifyListeners();
		}
	}

	private void checkInputs() {
		try {
			RootDataNode.checkInputs(this, dmc);
		}
		catch (DependentException e) {
			setValue(new MappingDependentException(this, e));
		}
	}
	
	public void futureUpdated(Future f) {
	    try {
            innerInit();
        }
        catch (Exception e) {
            this.setValue(new MappingException(this, e));
        }
    }
	
	public RootHandle getRoot() {
		return this;
	}

	public DSHandle getParent() {
		return null;
	}

	public synchronized Mapper getMapper() {
		if (initialized) {
			return mapper;
		}
        if (waitingMapperParam == null) {
            return null;
        }
        else {
            throw new FutureNotYetAvailable(waitingMapperParam);
        }
	}
	
	public Mapper getActualMapper() {
        return mapper;
    }

	public boolean isArray() {
		return true;
	}

    public void setValue(Object value) {
        super.setValue(value);
        initialized();
    }

    private synchronized void initialized() {
        initialized = true;
        waitingMapperParam = null;
        if (variableTracer.isEnabled()) {
            variableTracer.trace(getThread(), getLine(), getName() + " INITIALIZED " + mapper);
        }
    }
}
