package org.griphyn.vdl.mapping;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import org.griphyn.vdl.mapping.Mapper;

/** AbstractMapper provides an implementation of the Mapper interface to be
    used as a base class for writing other mappers. It provides handling
    for mapper properties in a simple fashion that should be suitable for
    most cases.
*/

public abstract class AbstractMapper implements Mapper {

	public static final Logger logger = Logger.getLogger(AbstractMapper.class);
	public static final MappingParam PARAM_INPUT = new MappingParam("input", Boolean.FALSE);
	protected Map params;

	public synchronized void setParam(String name, Object value) {
		if (params == null) {
			params = new HashMap();
		}
		params.put(name, value);
	}

	public synchronized Object getParam(String name) {
		if (params != null) {
			return params.get(name);
		}
		else {
			return null;
		}
	}

	public void setParams(Map params) {
		this.params = params;
	}

	public boolean exists(Path path) {
		if(logger.isDebugEnabled())
			logger.debug("checking for existence of "+path);
		boolean r = ((AbsFile) map(path)).exists();
		if(logger.isDebugEnabled()) {
			if(r) {
				logger.debug(""+path+" exists");
			} else {
				logger.debug(""+path+" does not exist");
			}
		}
		return r;
	}

}
