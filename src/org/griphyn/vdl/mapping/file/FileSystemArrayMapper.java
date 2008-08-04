package org.griphyn.vdl.mapping.file;

import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

public class FileSystemArrayMapper extends AbstractFileMapper {
	private Map filenames = new HashMap();
	private int count = 0;
	
	public Path rmap(String name) {
		if (name == null || name.equals("")) {
			return null;
		}
		String index = String.valueOf(count);
		filenames.put(index, name);
		Path p = Path.EMPTY_PATH;
		p = p.addFirst(index, true);
		++count;
		return p;
	}
	
	public PhysicalFormat map(Path path) {
		if (path.size()!=1) {
			return null;
		}
		if (!path.isArrayIndex(0)) {
			return null;
		}
		String location = PARAM_LOCATION.getStringValue(this);
		String index = path.getFirst();
		String filename = (String)filenames.get(index);
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
