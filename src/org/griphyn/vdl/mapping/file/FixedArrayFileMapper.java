package org.griphyn.vdl.mapping.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.InvalidMappingParameterException;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;


/** Maps a string (separated by space, comma or semicolon) of filenames to
    an array. */
public class FixedArrayFileMapper extends AbstractMapper {
	public static final MappingParam PARAM_FILES = new MappingParam("files");

	public FixedArrayFileMapper() {
		super();
	}

	public void setParams(Map params) {
		super.setParams(params);
		String cfiles = PARAM_FILES.getStringValue(this);
		if (cfiles == null) {
			throw new InvalidMappingParameterException("Missing required mapper parameter: "
					+ PARAM_FILES);
		}
		StringTokenizer st = new StringTokenizer(cfiles, " ,;");
		String[] files = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++) {
			files[i] = st.nextToken();
		}
		PARAM_FILES.setValue(this, files);
	}

	protected String[] getFiles() {
		return (String[]) PARAM_FILES.getValue(this);
	}

	public Collection existing() {
		List l = new ArrayList();
		for (int i = 0; i < getFiles().length; i++) {
			l.add(Path.EMPTY_PATH.addLast(String.valueOf(i), true));
		}
		return l;
	}

	public PhysicalFormat map(Path path) {
		if (!path.isArrayIndex(0)) {
			throw new IllegalArgumentException(path.toString());
		}
		else {
			int index = Integer.parseInt(path.getFirst());
			return new AbsFile(getFiles()[index]);
		}
	}

	public boolean isStatic() {
		return true;
	}
}
