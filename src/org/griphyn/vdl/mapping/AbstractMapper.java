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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.griphyn.vdl.mapping.file.FileGarbageCollector;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.type.Type;

/** AbstractMapper provides an implementation of the Mapper interface to be
    used as a base class for writing other mappers. It provides handling
    for mapper properties in a simple fashion that should be suitable for
    most cases.
*/

public abstract class AbstractMapper implements Mapper {

	public static final Logger logger = Logger.getLogger(AbstractMapper.class);
	
	private MappingParamSet params;
	private String baseDir;
	
    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public final Set<String> getSupportedParamNames() {
        Set<String> s = new HashSet<String>();
        getValidMappingParams(s);
        return s;
    }
    
    protected void getValidMappingParams(Set<String> s) {
    }
    
    protected abstract MappingParamSet newParams();
    
    @SuppressWarnings("unchecked")
    protected <T> T getParams() {
        return (T) params;
    }
    
    @Override
    public void setParameters(GenericMappingParamSet params) {
        this.params = newParams();
        this.params.setAll(params);
    }

    @Override
    public AbstractDataNode getFirstOpenParameter() {
        if (params != null) {
            return params.getFirstOpen();
        }
        else {
            return null;
        }
    }

    @Override
    public void initialize(RootHandle root) {
        params.unwrapPrimitives();
    }

    @Override
	public boolean exists(Path path) throws InvalidPathException {
		if (logger.isDebugEnabled()) {
			logger.debug("checking for existence of " + path);
		}
		boolean r = ((AbsFile) map(path)).exists();
		if(logger.isDebugEnabled()) {
			if (r) {
				logger.debug(path + " exists");
			} else {
				logger.debug(path + " does not exist");
			}
		}
		return r;
	}

    @Override
	public boolean canBeRemapped(Path path) {
	    return false;
	}

    @Override
    public void remap(Path path, Mapper sourceMapper, Path sourcePath) throws InvalidPathException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPersistent(Path path) {
        // persistent unless explicitly overridden
        return true;
    }
            
    public abstract String getName();
    
    @Override
    public String toString() {
        Object desc = getName();
        if (desc == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(desc);
        sb.append("; ");
        if (params != null) {
            params.toString(sb);
        }
        sb.append('>');
        return sb.toString();
    }

    @Override
    public Collection<AbsFile> getPattern(Path path, Type type) {
        if (isStatic()) {
            return null;
        }
        else {
            throw new UnsupportedOperationException(this.getClass().getName() + ".getPattern()");
        }
    }
    
    @Override
    public boolean supportsCleaning() {
        return false;
    }

    @Override
    public void fileCleaned(PhysicalFormat pf) {
    }
}
