package org.griphyn.vdl.mapping;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.griphyn.vdl.mapping.file.FileGarbageCollector;

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

    public void remap(Path path, Mapper sourceMapper, Path sourcePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clean(Path path) {
        // no cleaning by default
    }

    @Override
    public boolean isPersistent(Path path) {
        // persistent unless explicitly overridden
        return true;
    }
    
    protected void ensureCollectionConsistency(Mapper sourceMapper, Path sourcePath) {
        // if remapping from a persistent mapper, then file removal
        // should be avoided
        PhysicalFormat pf = sourceMapper.map(sourcePath);
        if (sourceMapper.isPersistent(sourcePath)) {
            FileGarbageCollector.getDefault().markAsPersistent(pf);
        }
        else {
            FileGarbageCollector.getDefault().increaseUsageCount(pf);
        }
    }
}
