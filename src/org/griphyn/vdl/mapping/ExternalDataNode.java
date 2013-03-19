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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.Loader;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.type.impl.FieldImpl;

public class ExternalDataNode extends AbstractDataNode {

	static final String DATASET_URI_PREFIX = "dataset:external:";

	public static final Logger logger = Logger.getLogger(ExternalDataNode.class);
	
	public static final MappingParam PARAM_PREFIX = new MappingParam("prefix", null);

	private static long datasetIDCounter = 850000000000l;

	private static final String datasetIDPartialID = Loader.getUUID();
	
	private MappingParamSet params;

	public ExternalDataNode() {
	    super(new FieldImpl("", Types.EXTERNAL));
	}

	@Override
    public void init(MappingParamSet params) {
        this.params = params;
    }

    public boolean isRestartable() {
		return true;
	}

	public DSHandle getRoot() {
		return this;
	}

	public DSHandle getField(Path path) throws InvalidPathException {
		if (path.isEmpty()) {
			return this;
		} 
		else {
			throw new InvalidPathException(path, this);
		}
	}
	
	protected void getFields(List<DSHandle> fields, Path path) throws InvalidPathException {
	    // nothing
	}

	public void set(DSHandle handle) {
		throw new UnsupportedOperationException(this.getDisplayableName() + " is an external dataset and cannot be set");
	}

	public Object getValue() {
	    logger.warn("getValue called on an external dataset");
	    return null;
	}

	public Map<Comparable<?>, DSHandle> getArrayValue() {
	    throw new UnsupportedOperationException("cannot get value of external dataset");
	}

	public boolean isArray() {
		return false;
	}

	public Collection<Path> getFringePaths() throws HandleOpenException {
	    return Collections.singletonList(Path.EMPTY_PATH);
	}

	public Path getPathFromRoot() {
		return Path.EMPTY_PATH;
	}

	public Mapper getMapper() {
		return null;
	}

	protected String makeIdentifierURIString() {
		datasetIDCounter++;
		return DATASET_URI_PREFIX + datasetIDPartialID + ":" + datasetIDCounter; 
	}

	public DSHandle createDSHandle(String fieldName) {
	    throw new UnsupportedOperationException("cannot create new field in external dataset");
	}

	public DSHandle getParent() {
	    return null;
	}

    @Override
    protected AbstractDataNode getParentNode() {
        return null;
    }

    public String getParam(MappingParam p) {
        if (params == null) {
            return null;
        }
        return (String) params.get(p);
    }
}
