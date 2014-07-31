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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.FileSystemLister;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

/** Maps every Path to a single file name (specified by the "file" parameter).
  */
public class SingleFileMapper extends AbstractMapper {
    public static final String NAME = "SingleFileMapper";
	
	@Override
    protected void getValidMappingParams(Set<String> s) {
	    s.addAll(SingleFileMapperParams.NAMES);
        super.getValidMappingParams(s);
    }

    public SingleFileMapper() {
		super();
	}

    @Override
    protected MappingParamSet newParams() {
        return new SingleFileMapperParams();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Collection<Path> existing() {
        SingleFileMapperParams cp = getParams();
		if (cp.getFile().exists()) {
			return Arrays.asList(new Path[] {Path.EMPTY_PATH});
		}
		else {
			return Collections.emptyList();
		}
	}
    
    @Override
    public Collection<Path> existing(FileSystemLister l) {
        throw new UnsupportedOperationException();
    }

    @Override
	public PhysicalFormat map(Path path) {
        SingleFileMapperParams cp = getParams();
        return cp.getFile();
	}

	public boolean isStatic() {
		return true;
	}
}
