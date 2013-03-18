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

import java.util.HashSet;
import java.util.Set;

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
	
	protected void getValidMappingParams(Set<String> s) {
	    addParams(s, PARAM_INPUT);
	}
	
	protected void addParams(Set<String> s, MappingParam... params) {
	    for (MappingParam p : params) {
	        s.add(p.getName());
	    }
    }
	
    @Override
    public final Set<String> getSupportedParamNames() {
        Set<String> s = new HashSet<String>();
        getValidMappingParams(s);
        return s;
    }

    protected MappingParamSet params;

	public synchronized void setParam(MappingParam param, Object value) {
		if (params == null) {
			params = new MappingParamSet();
		}
		params.set(param, value);
	}

	public synchronized Object getParam(MappingParam param) {
		if (params != null) {
			return params.get(param);
		}
		else {
			return null;
		}
	}

	public void setParams(MappingParamSet params) throws HandleOpenException {
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

    public void clean(Path path) {
        // no cleaning by default
    }

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
