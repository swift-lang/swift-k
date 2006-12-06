/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.griphyn.vdl.mapping.Path;

public class FixedFileMapper extends AbstractFileMapper {
    public static final String PARAM_FILE = "file";
    
	private String file;

	public FixedFileMapper() {
		super();
	}

	public void setParams(Map params) {
		super.setParams(params);
		file = (String) params.get(PARAM_FILE);
	}

	public Collection existing() {
		if (new File((String) getParams().get("file")).exists()) {
			return Arrays.asList(new Path[] {Path.EMPTY_PATH});
		}
		else {
			return Collections.EMPTY_LIST;
		}
	}

	public String map(Path path) {
		return file;
	}

	public Path rmap(String name) {
		return Path.EMPTY_PATH;
	}
	
	public boolean isStatic() {
		return true;
	}
}
