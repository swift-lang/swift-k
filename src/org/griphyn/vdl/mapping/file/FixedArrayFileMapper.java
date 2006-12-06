/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.griphyn.vdl.mapping.Path;

public class FixedArrayFileMapper extends AbstractFileMapper {
	public static final String PARAM_FILES = "files";

	private String[] files;
	private Map rmap;

	public FixedArrayFileMapper() {
		super();
	}

	public void setParams(Map params) {
		super.setParams(params);
		String cfiles = (String) params.get(PARAM_FILES);
		if (cfiles == null) {
			throw new IllegalArgumentException("Missing required mapper parameter: " + PARAM_FILES);
		}
		StringTokenizer st = new StringTokenizer(cfiles, " ,;");
		files = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++) {
			files[i] = st.nextToken();
		}
	}

	public Collection existing() {
		List l = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			l.add(Path.EMPTY_PATH.addLast(String.valueOf(i), true));
		}
		return l;
	}

	public String map(Path path) {
		if (!path.isArrayIndex(0)) {
			throw new IllegalArgumentException(path.toString());
		}
		else {
			int index = Integer.parseInt(path.getFirst());
			return files[index];
		}
	}

	public Path rmap(String name) {
		synchronized (this) {
			if (rmap == null) {
				rmap = new HashMap();
				for (int i = 0; i < files.length; i++) {
					rmap.put(files[i], String.valueOf(i));
				}
			}
		}
		String index = (String) rmap.get(name);
		if (index == null) {
			throw new IllegalArgumentException("This mapper is not aware of the specified file ("
					+ name + ")");
		}
		return Path.EMPTY_PATH.addLast(index, true);
	}

	public boolean isStatic() {
		return true;
	}
}
