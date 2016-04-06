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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.FileSystemLister;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.type.Type;

public class FileSystemArrayMapper extends AbstractFileMapper {    
    private Map<Comparable<?>, String> filenames;
    private int count = 0;

    @Override
    public String getName() {
        return "FilesysMapper";
    }

    @Override
    public Collection<Path> existing(FileSystemLister fsl) {
        filenames = new HashMap<Comparable<?>, String>();
        return super.existing(fsl);
    }

    @Override
	protected Path rmap(AbstractFileMapperParams cp, AbsFile file) {
		if (file == null) {
		    // This test is fishy
			return null;
		}
		Path p = Path.EMPTY_PATH;
		p = p.addFirst(count++, true);
		filenames.put(p.getFirst(), file.getPath());
		return p;
	}
	
    @Override
	public PhysicalFormat map(Path path) throws InvalidPathException {
        if (filenames == null) {
            throw new InvalidPathException("The FilesysMapper could not map " + path);
        }
		if (path.size() != 1) {
			throw new InvalidPathException(path);
		}
		if (!path.isArrayIndex(0)) {
			throw new InvalidPathException(path);
		}
		AbstractFileMapperParams cp = getParams();
		String location = cp.getLocation();
		Object index = path.getFirst();
		String filename = filenames.get(index);
		if (filename == null) {
			return null;
		}
		return newFile(filename);
	}

    @Override
    public Collection<AbsFile> getPattern(Path path, Type type) {
        if (!type.isArray()) {
            throw new IllegalArgumentException("Cannot use a non-array type with the FilesysMapper");
        }
        AbstractFileMapperParams cp = getParams();
        String location = defaultValue(cp.getLocation(), "./");
        String prefix = defaultValue(cp.getPrefix(), "");
        String suffix = defaultValue(cp.getSuffix(), "");
        String pattern = defaultValue(cp.getPattern(), null);
        if (pattern != null) {
            return Collections.singletonList(newFile(location + "/" + pattern));
        }
        else {
            return Collections.singletonList(newFile(location + "/" + prefix + "*" + suffix));
        }
    }

    private String defaultValue(String s, String d) {
        return s == null ? d : s;
    }
}
