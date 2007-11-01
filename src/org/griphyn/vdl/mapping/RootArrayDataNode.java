package org.griphyn.vdl.mapping;

import java.util.Map;

import org.griphyn.vdl.karajan.VDL2FutureException;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class RootArrayDataNode extends ArrayDataNode implements DSHandleListener {

	private Mapper mapper;
	private Map params;

	/**
	 * Instantiate a root array data node with elements of the array
	 * having the specified type.
	 */
	public RootArrayDataNode(Type type) {
		super(Field.Factory.newInstance(), null, null);
		getField().setType(type.arrayType());
	}

	public void init(Map params) {
		this.params = params;
		String desc = (String) params.get("descriptor");
		if (desc == null) {
			return;
		}
		try {
			mapper = MapperFactory.getMapper(desc, params);
			checkInputs();
			getField().setName(PARAM_PREFIX.getStringValue(mapper));
		}
		catch (InvalidMapperException e) {
			throw new RuntimeException(e);
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
		checkInputs();
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

	public Mapper getMapper() {
		return mapper;
	}

	public boolean isArray() {
		return true;
	}

}
