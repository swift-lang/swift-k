/*
 * Created on Jan 8, 2007
 */
package org.griphyn.vdl.mapping;

import java.util.Map;

import org.griphyn.vdl.karajan.VDL2FutureException;

public class MappingParam {
	private final String name;

	public MappingParam(String name) {
		this.name = name;
	}

	public Object getValue(Mapper mapper) {
		Object value = mapper.getParam(name);
		if (value instanceof DSHandle) {
			DSHandle handle = (DSHandle) value;
			checkHandle(handle);
			return handle.getValue();
		}
		else {
			return value;
		}
	}

	private void checkHandle(DSHandle handle) {
		if (!handle.isClosed()) {
			throw new VDL2FutureException(handle);
		}
		Object value = handle.getValue();
	}

	public String getFileName(Mapper mapper) {
		Object value = mapper.getParam(name);
		if (value instanceof DSHandle) {
			DSHandle handle = (DSHandle) value;
			checkHandle(handle);
			return handle.getFilename();
		}
		else if (value instanceof String) {
			return (String) value;
		}
		else {
			throw new RuntimeException("Could not figure out file name for value " + value);
		}
	}

	public String getStringValue(Mapper mapper) {
		return (String) getValue(mapper);
	}

	public void setValue(Mapper mapper, Object value) {
		mapper.setParam(name, value);
	}

	public boolean isPresent(Mapper mapper) {
		return mapper.getParam(name) != null;
	}

	public boolean isPresent(Map map) {
		return map.containsKey(name);
	}

	public boolean getBooleanValue(Mapper mapper) {
		Object value = getValue(mapper);
		if (value instanceof String) {
			return Boolean.valueOf((String) value).booleanValue();
		}
		else if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		else {
			return false;
		}
	}

	public int getIntValue(Mapper mapper) {
		Object value = getValue(mapper);
		if (value instanceof String) {
			return Integer.parseInt((String) value);
		}
		else if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}
		else {
			throw new NumberFormatException(String.valueOf(value));
		}
	}
	
	public Object getValue(Map map) {
		return map.get(name); 
	}
	
	public void setValue(Map map, Object value) {
		map.put(name, value);
	}
}
