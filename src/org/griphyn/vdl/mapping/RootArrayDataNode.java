package org.griphyn.vdl.mapping;

import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class RootArrayDataNode extends ArrayDataNode implements FutureListener {

    Logger logger = Logger.getLogger(RootArrayDataNode.class);
    
	private boolean initialized = false;
	private Mapper mapper;
	private Map<String, Object> params;
	private AbstractDataNode waitingMapperParam;

	/**
	 * Instantiate a root array data node with specified type.
	 */
	public RootArrayDataNode(Type type) {
		super(Field.Factory.createField(null, type), null, null);
	}

	public void init(Map<String, Object> params) {
		this.params = params;
		if (this.params == null) {
			initialized();
		}
		else {
			innerInit();
		}
	}

	private synchronized void innerInit() {
	    logger.debug("innerInit: " + this);
	    
	    for (Map.Entry<String, Object> entry : params.entrySet()) {
			Object v = entry.getValue();
			if (v instanceof AbstractDataNode && !((AbstractDataNode) v).isClosed()) {
				waitingMapperParam = (AbstractDataNode) v;
				waitingMapperParam.getFutureWrapper().addModificationAction(this, null);
				return;
			}
		}
		String desc = (String) params.get("descriptor");
		if (desc == null) {
			initialized();
			return;
		}
		try {
			mapper = MapperFactory.getMapper(desc, params);
			getField().setName(PARAM_PREFIX.getStringValue(mapper));
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
			RootDataNode.checkInputs(params, mapper, this);
		}
		catch (DependentException e) {
			setValue(new MappingDependentException(this, e));
			closeShallow();
		}
	}
	
	public void futureModified(Future f, VariableStack stack) {
	    innerInit();
    }

	public String getParam(String name) {
		if (params == null) {
			return null;
		}
		return (String) params.get(name);
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
    }
}
