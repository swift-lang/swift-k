/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.Map;
import java.util.HashMap;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.AbsFile;

import org.griphyn.vdl.mapping.MappingParam;

public class SimpleFileMapper extends AbstractFileMapper {
	public static final MappingParam PARAM_PADDING = new MappingParam("padding", new Integer(4));
	private Map filenames = new HashMap();
	private int count = 0;
        private String isinput;

	public SimpleFileMapper() {
		super();
		

	}

	public void setParams(Map params) {
		super.setParams(params);
		int precision = PARAM_PADDING.getIntValue(this);
		setElementMapper(new DefaultFileNameElementMapper(precision));
		this.isinput = (String)params.get("input");
	}

    //all input filesnames will be parsed as strings and given numeric index

	public Path rmap(String name) {
	    if(this.isinput == null){
		return super.rmap(name);
	    }
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
	    if(this.isinput == null){
		return super.map(path);
	    }
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
