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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidMappingParameterException;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.type.Types;


/** Maps a string (separated by space, comma or semicolon) of filenames to
    an array. */
public class FixedArrayFileMapper extends AbstractMapper {
	public static final MappingParam PARAM_FILES = new MappingParam("files");
	
	
	@Override
    protected void getValidMappingParams(Set<String> s) {
	    addParams(s, PARAM_FILES);
        super.getValidMappingParams(s);
    }
	
	private List<String> files;

	public FixedArrayFileMapper() {
		super();
	}

	public void setParams(MappingParamSet params) {
		super.setParams(params);
		DSHandle dn = (DSHandle) PARAM_FILES.getRawValue(this);
		if (dn == null) {
		    throw new InvalidMappingParameterException("Missing required mapper parameter: "
                    + PARAM_FILES);
		}
		if (Types.STRING.equals(dn.getType())) {
		    String cfiles = (String) dn.getValue();
        
            StringTokenizer st = new StringTokenizer(cfiles, " ,;");
            String[] files = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                files[i] = st.nextToken();
            }
            this.files = Arrays.asList(files);
		}
		else if (dn.getType().isArray() && Types.STRING.equals(dn.getType().itemType())) {
		    files = new ArrayList<String>();
            Map<?, DSHandle> m = dn.getArrayValue();
            // must keep order
            @SuppressWarnings("unchecked")
            Set<Comparable<?>> s = new TreeSet<Comparable<?>>((Set<Comparable<?>>) m.keySet());
            Iterator<?> i = s.iterator();
            while(i.hasNext()) {
                Comparable<?> nextKey = (Comparable<?>) i.next();
                files.add((String) m.get(nextKey).getValue());
            }
		}
		else {
		    throw new InvalidMappingParameterException("Unrecognized value for "
                    + PARAM_FILES + " parameter: " + dn.getType() + ". Valid values are a string or an array of strings.");
		}
	}

	public Collection<Path> existing() {
		List<Path> l = new ArrayList<Path>();
		for (int i = 0; i < files.size(); i++) {
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
		        return new AbsFile(files.get(index));
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
