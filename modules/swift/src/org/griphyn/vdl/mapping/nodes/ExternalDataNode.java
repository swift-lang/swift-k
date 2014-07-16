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


package org.griphyn.vdl.mapping.nodes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.Loader;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.type.impl.FieldImpl;

public class ExternalDataNode extends AbstractFutureNonCompositeDataNode implements RootHandle {

	static final String DATASET_URI_PREFIX = "dataset:external:";

	public static final Logger logger = Logger.getLogger(ExternalDataNode.class);
	
	private static long datasetIDCounter = 850000000000l;

	private static final String datasetIDPartialID = Loader.getUUID();
	
    // previously in mapper params
    private int line = -1;
    private LWThread thread;
    private boolean input;

	
	public ExternalDataNode(String name) {
	    super(new FieldImpl(name, Types.EXTERNAL));
	}
	
	public ExternalDataNode(Field field) {
        super(field);
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public void setThread(LWThread thread) {
        this.thread = thread;
    }

    public LWThread getThread() {
        return thread;
    }

	public String getName() {
        return (String) getField().getId();
    }
	
	@Override
    public void setName(String name) {
        getField().setId(name);
    }

	@Override
    public void init(Mapper mapper) {
    }

    @Override
    public void mapperInitialized(Mapper mapper) {
    }

    public boolean isRestartable() {
		return true;
	}

	public RootHandle getRoot() {
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

    @Override
    public synchronized void closeDeep() {
        if (!this.isClosed()) {
            /*
             * Need to override this and set a value since 
             * this is skipped by the normal stageout mechanism which
             * does that
             */
            this.setValue(FILE_VALUE);
        }
    }

    @Override
    public Mapper getActualMapper() {
        return null;
    }

    @Override
    public void closeArraySizes() {
    }

    @Override
    protected void getFringePaths(List<Path> list, Path myPath) throws HandleOpenException {
    }

    @Override
    protected void getLeaves(List<DSHandle> list) throws HandleOpenException {
    }
}
