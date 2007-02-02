/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;

public class SingleFileMapper extends AbstractFileMapper {
    public static final MappingParam PARAM_FILE = new MappingParam("file");

	public SingleFileMapper() {
		super();
	}

	public Collection existing() {
		if (new File(PARAM_FILE.getStringValue(this)).exists()) {
			return Arrays.asList(new Path[] {Path.EMPTY_PATH});
		}
		else {
			return Collections.EMPTY_LIST;
		}
	}

	public String map(Path path) {
		return PARAM_FILE.getStringValue(this);
	}

	public Path rmap(String name) {
		return Path.EMPTY_PATH;
	}
	
	public boolean isStatic() {
		return true;
	}
}
