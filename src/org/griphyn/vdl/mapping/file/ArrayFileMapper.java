package org.griphyn.vdl.mapping.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.FileSystemLister;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;


public class ArrayFileMapper extends AbstractMapper {
    static Logger logger = Logger.getLogger(ArrayFileMapper.class);
    
    
	@Override
    public String getName() {
        return "ArrayMapper";
    }

    @Override
    protected void getValidMappingParams(Set<String> s) {
	    s.addAll(ArrayFileMapperParams.NAMES);
        super.getValidMappingParams(s);
    }

	@Override
	public Collection<Path> existing() {
	    ArrayFileMapperParams cp = getParams();
		List<Path> l = new ArrayList<Path>();
		DSHandle dn = cp.getFiles();
		Map<?, DSHandle> m = dn.getArrayValue();
		Set<?> s = m.keySet();
		Iterator<?> i = s.iterator();
		while(i.hasNext()) {
			Comparable<?> nextKey = (Comparable<?>) i.next();
			l.add(Path.EMPTY_PATH.addLast(nextKey, true));
		}
		return l;
	}

	@Override
    public Collection<Path> existing(FileSystemLister l) {
        throw new UnsupportedOperationException();
    }

    @Override
	public PhysicalFormat map(Path path) {
		if (path.isEmpty()) {
			throw new IllegalArgumentException("Path cannot be empty");
		}
		if (!path.isArrayIndex(0)) {
			throw new IllegalArgumentException("First element of path "+path.toString()+" must be an array index");
		}
		ArrayFileMapperParams cp = getParams();
        // we could typecheck more elegantly here to make sure that
        // we really do have an array of strings as parameter.
        DSHandle dn = cp.getFiles();
        assert(dn.isClosed());
        logger.debug("dn: " + dn);
        
        DSHandle srcNode = null;
        try {
        	srcNode = dn.getField(path);
        } 
        catch (InvalidPathException e) {
        	logger.error("Invalid path exception "+e+" for path "+path,e);
        	return null;
        }
        String returnValue = srcNode.getValue().toString();
        return new AbsFile(returnValue);
	}

	@Override
    public MappingParamSet newParams() {
        return new ArrayFileMapperParams();
    }

    public boolean isStatic() {
		return true;
	}
	
	public String toString() { 
	    return "ArrayFileMapper";
	}
}
