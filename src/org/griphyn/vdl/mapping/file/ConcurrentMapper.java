/*
 * Created on Sep 20, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

public class ConcurrentMapper extends AbstractFileMapper {
	public static final MappingParam PARAM_THREAD_PREFIX = new MappingParam("thread_prefix", "");
	
	private Map remappedPaths;

	public ConcurrentMapper() {
		super(new ConcurrentElementMapper());
	}

	public void setParams(Map params) {
		String prefix = PARAM_PREFIX.getStringValue(params);
		prefix = "_concurrent/" + (prefix == null ? "" : prefix + "-") + 
		    PARAM_THREAD_PREFIX.getValue(params);
		PARAM_PREFIX.setValue(params, prefix);
		super.setParams(params);
	}
	
    public synchronized Collection existing() {
        Collection c = super.existing();
        if (remappedPaths != null) {
            Set s = new HashSet(c);
            s.add(remappedPaths.keySet());
            return s;
        }
        else {
            return c;
        }
    }

    public synchronized PhysicalFormat map(Path path) {
        if (remappedPaths != null) {
            Object o = remappedPaths.get(path);
            if (o != null) {
                return (PhysicalFormat) o;
            }
        }
        return super.map(path);
    }

    public Path rmap(String name) {
        throw new UnsupportedOperationException();
    }

    public boolean canBeRemapped(Path path) {
        return true;
    }

    public synchronized void remap(Path path, PhysicalFormat file) {
        if (remappedPaths == null) {
            remappedPaths = new HashMap();
        }
        remappedPaths.put(path, file);
    }
}