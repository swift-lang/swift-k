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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.lib.Tracer;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class RootArrayDataNode extends ArrayDataNode implements FutureListener {

    Logger logger = Logger.getLogger(RootArrayDataNode.class);
    
	private boolean initialized = false;
	private Mapper mapper;
	private MappingParamSet params;
	private AbstractDataNode waitingMapperParam;
	private DuplicateMappingChecker dmc;
	
	private static final Tracer tracer = Tracer.getTracer("VARIABLE");

	/**
	 * Instantiate a root array data node with specified type.
	 */
	public RootArrayDataNode(Type type) {
		super(Field.Factory.createField(null, type), null, null);
	}
	
	public RootArrayDataNode(Type type, DuplicateMappingChecker dmc) {
	    this(type);
	    this.dmc = dmc;
	}

	public void init(MappingParamSet params) {
		this.params = params;
		if (this.params == null) {
			initialized();
		}
		else {
			innerInit();
		}
	}

	private synchronized void innerInit() {
		if (logger.isDebugEnabled()) {
		    logger.debug("innerInit: " + this);
		}
	    
		waitingMapperParam = params.getFirstOpenParamValue();
        if (waitingMapperParam != null) {
            waitingMapperParam.getFutureWrapper().addModificationAction(this, null);
            if (tracer.isEnabled()) {
                tracer.trace(getThread(), getDeclarationLine(), getDisplayableName() + " WAIT " 
                    + Tracer.getVarName(waitingMapperParam));
            }
            return;
        }
	    
		String desc = (String) params.get(MappingParam.SWIFT_DESCRIPTOR);
		if (desc == null) {
			initialized();
			return;
		}
		try {
			mapper = MapperFactory.getMapper(desc, params);
			getField().setId(PARAM_PREFIX.getStringValue(mapper));
			initialized();
			checkInputs();
		}
		catch (InvalidMapperException e) {
			throw new RuntimeException(e);
		}
		if (isClosed()) {
		    notifyListeners();
		}
	}

	private void checkInputs() {
		try {
			RootDataNode.checkInputs(params, mapper, this, dmc);
		}
		catch (DependentException e) {
			setValue(new MappingDependentException(this, e));
			closeShallow();
		}
	}
	
	public void futureModified(Future f, VariableStack stack) {
	    innerInit();
    }

	public String getParam(MappingParam p) {
		if (params == null) {
			return null;
		}
		return (String) params.get(p);
	}

	public DSHandle getRoot() {
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
            throw new FutureNotYetAvailable(waitingMapperParam.getFutureWrapper());
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
        if (tracer.isEnabled()) {
            tracer.trace(getThread(), getDeclarationLine(), getDisplayableName() + " INITIALIZED " + params);
        }
    }
}
