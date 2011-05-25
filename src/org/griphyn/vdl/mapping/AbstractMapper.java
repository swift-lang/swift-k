package org.griphyn.vdl.mapping;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/** AbstractMapper provides an implementation of the Mapper interface to be
    used as a base class for writing other mappers. It provides handling
    for mapper properties in a simple fashion that should be suitable for
    most cases.
*/

public abstract class AbstractMapper implements Mapper {

	public static final Logger logger = Logger.getLogger(AbstractMapper.class);
	public static final MappingParam PARAM_INPUT = new MappingParam("input", Boolean.FALSE);
	protected Map<String, Object> params;

	public synchronized void setParam(String name, Object value) {
		if (params == null) {
			params = new HashMap<String, Object>();
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

	public void setParams(Map<String, Object> params) {
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

	public boolean canBeRemapped(Path path) {
	    return false;
	}

    public void remap(Path path, PhysicalFormat file) {
        throw new UnsupportedOperationException();
    }
}
