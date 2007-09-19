package org.griphyn.vdl.mapping.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

public class ArrayFileMapper extends AbstractMapper {
	public static final MappingParam PARAM_FILES = new MappingParam("files");

	public Collection existing() {
		List l = new ArrayList();
		DSHandle dn = (DSHandle) PARAM_FILES.getRawValue(this);
		Map m = dn.getArrayValue();
		Set s = m.keySet();
		Iterator i = s.iterator();
		while(i.hasNext()) {
			String nextKey = i.next().toString();
			l.add(Path.EMPTY_PATH.addLast(nextKey,true));
		}
		return l;
	}

	public PhysicalFormat map(Path path) {

		if (!path.isArrayIndex(0)) {
			throw new IllegalArgumentException(path.toString());
		}
		else {
			int index = Integer.parseInt(path.getFirst());

			// we could typecheck more elegantly here to make sure that
			// we really do have an array of strings as parameter.
			DSHandle dn = (DSHandle) PARAM_FILES.getRawValue(this);

			DSHandle srcNode = null;
			try {
				srcNode = dn.getField(path);
			} catch(InvalidPathException e) {
				return null;
			}
			String returnValue = srcNode.getValue().toString();
			return new AbsFile(returnValue);
		}
	}

	public boolean isStatic() {
		return false;
	}
}
