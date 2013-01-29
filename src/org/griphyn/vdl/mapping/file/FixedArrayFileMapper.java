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
import java.util.StringTokenizer;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidMappingParameterException;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;


/** Maps a string (separated by space, comma or semicolon) of filenames to
    an array. */
public class FixedArrayFileMapper extends AbstractMapper {
	public static final MappingParam PARAM_FILES = new MappingParam("files");

	private String[] files;
	
	public FixedArrayFileMapper() {
		super();
	}

	public void setParams(MappingParamSet params) throws HandleOpenException {
		super.setParams(params);
		String cfiles = PARAM_FILES.getStringValue(this);
		if (cfiles == null) {
			throw new InvalidMappingParameterException("Missing required mapper parameter: "
					+ PARAM_FILES);
		}
		StringTokenizer st = new StringTokenizer(cfiles, " ,;");
		files = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++) {
			files[i] = st.nextToken();
		}
		params.set(PARAM_FILES, files);
	}

	protected String[] getFiles() {
		return files;
	}

	public Collection<Path> existing() {
		List<Path> l = new ArrayList<Path>();
		for (int i = 0; i < getFiles().length; i++) {
			l.add(Path.EMPTY_PATH.addLast(i, true));
		}
		return l;
	}

	public PhysicalFormat map(Path path) {
		if (!path.isArrayIndex(0)) {
			throw new IllegalArgumentException(path.toString());
		}
		else {
		    Object o = path.getFirst();
		    if (o instanceof Integer) {
		        int index = ((Integer) o).intValue();
		        return new AbsFile(getFiles()[index]);
		    }
		    else {
		        throw new IllegalArgumentException("The fixed array mapper can only be used with an int key array");
		    }
		}
	}

	public boolean isStatic() {
		return true;
	}
}
