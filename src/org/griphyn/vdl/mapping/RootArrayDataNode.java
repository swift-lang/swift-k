package org.griphyn.vdl.mapping;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.VDL2FutureException;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class RootArrayDataNode extends ArrayDataNode implements DSHandleListener {

    Logger logger = Logger.getLogger(RootArrayDataNode.class);
    
	private boolean initialized = false;
	private Mapper mapper;
	private Map<String, Object> params;
	private DSHandle waitingMapperParam;

	/**
	 * Instantiate a root array data node with specified type.
	 */
	public RootArrayDataNode(Type type) {
		super(Field.Factory.newInstance(), null, null);
		getField().setType(type);
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
		Iterator i = params.entrySet().iterator();
		while(i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			Object v = entry.getValue();
			if (v instanceof DSHandle && !((DSHandle) v).isClosed()) {
				waitingMapperParam = (DSHandle) v;
				waitingMapperParam.addListener(this);
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
		catch (VDL2FutureException e) {
			e.getHandle().addListener(this);
		}
		catch (DependentException e) {
			setValue(new MappingDependentException(this, e));
			closeShallow();
		}
	}

	public void handleClosed(DSHandle handle) {
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
        assert(waitingMapperParam != null);
        throw new VDL2FutureException(waitingMapperParam);
	}

	public boolean isArray() {
		return true;
	}

    public void setValue(Object value) {
        super.setValue(value);
        initialized();
    }

    private void initialized() {
        initialized = true;
    }
}
