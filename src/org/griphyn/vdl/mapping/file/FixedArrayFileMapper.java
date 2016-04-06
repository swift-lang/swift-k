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


package org.griphyn.vdl.mapping.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.FileSystemLister;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Types;


/** Maps a string (separated by space, comma or semicolon) of filenames to
    an array. */
public class FixedArrayFileMapper extends AbstractMapper {

    @Override
    protected void getValidMappingParams(Set<String> s) {
	    s.addAll(FixedArrayFileMapperParams.NAMES);
        super.getValidMappingParams(s);
    }
	
	private String[] files;
	
	public FixedArrayFileMapper() {
		super();
	}
	
    @Override
    public String getName() {
        return "FixedArrayMapper";
    }


    @Override
	public void initialize(RootHandle root) {
		super.initialize(root);
		FixedArrayFileMapperParams cp = getParams();
		DSHandle files = cp.getFiles();
		if (files.getType().isArray() && Types.STRING.equals(files.getType().itemType())) {
		    int i = 0;
		    Collection<DSHandle> a = files.getArrayValue().values();
		    this.files = new String[a.size()];
		    for (DSHandle n : a) {
		        this.files[i] = (String) n.getValue();
		        i++;
		    }
		}
		else if (Types.STRING.equals(files.getType())) {
		    String v = (String) files.getValue();
		    this.files = v.split("[\\s,;]+");
		}
	}

	protected String[] getFiles() {
		return files;
	}

	@Override
	public Collection<Path> existing() {
		List<Path> l = new ArrayList<Path>();
		for (int i = 0; i < files.length; i++) {
			l.add(Path.EMPTY_PATH.addLast(i, true));
		}
		return l;
	}
	
	@Override
    public Collection<Path> existing(FileSystemLister l) {
        throw new UnsupportedOperationException();
    }

	@Override
	public PhysicalFormat map(Path path) throws InvalidPathException {
		if (!path.isArrayIndex(0)) {
			throw new InvalidPathException(path);
		}
		else {
		    Object o = path.getFirst();
		    if (o instanceof Integer) {
		        int index = ((Integer) o).intValue();
		        return newFile(getFiles()[index]);
		    }
		    else {
		        throw new IllegalArgumentException("The fixed array mapper can only be used with an int key array");
		    }
		}
	}

	public boolean isStatic() {
		return true;
	}

    @Override
    protected MappingParamSet newParams() {
        return new FixedArrayFileMapperParams();
    }
}
