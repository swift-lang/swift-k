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

import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

public class FileSystemArrayMapper extends AbstractFileMapper {    
    private Map<Object, String> filenames = new HashMap<Object, String>();
    private int count = 0;

    @Override
    public String getName() {
        return "FilesysMapper";
    }

    @Override
	public Path rmap(AbstractFileMapperParams cp, String name) {
		if (name == null || name.equals("")) {
			return null;
		}
		filenames.put(count, name);
		Path p = Path.EMPTY_PATH;
		p = p.addFirst(count, true);
		++count;
		return p;
	}
	
    @Override
	public PhysicalFormat map(Path path) {
		if (path.size()!=1) {
			return null;
		}
		if (!path.isArrayIndex(0)) {
			return null;
		}
		AbstractFileMapperParams cp = getParams();
		String location = cp.getLocation();
		Object index = path.getFirst();
		String filename = filenames.get(index);
		if (filename == null) {
			return null;
		}
		if (location != null) {
			if (!location.endsWith("/")) {
				filename = location + '/' + filename;
			} else {
				filename = location + filename;
			}
		}
		return new AbsFile(filename);
	}
}
