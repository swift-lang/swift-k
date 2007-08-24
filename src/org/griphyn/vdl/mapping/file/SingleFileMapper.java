package org.griphyn.vdl.mapping.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

/** Maps every Path to a single file name (specified by the "file" parameter),
  *  and every filename to the empty path.
  */
public class SingleFileMapper extends AbstractFileMapper {

	public static final MappingParam PARAM_FILE = new MappingParam("file");

	public SingleFileMapper() {
		super();
	}

	public Collection existing() {
		if (new AbsFile(PARAM_FILE.getStringValue(this)).exists()) {
			return Arrays.asList(new Path[] {Path.EMPTY_PATH});
		}
		else {
			return Collections.EMPTY_LIST;
		}
	}

	public PhysicalFormat map(Path path) {
		return new AbsFile(PARAM_FILE.getStringValue(this));
	}

	public Path rmap(String name) {
		return Path.EMPTY_PATH;
	}
	
	public boolean isStatic() {
		return true;
	}
}
