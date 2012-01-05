/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.List;
import java.util.Map;

import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureList;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.ArrayIndexFutureList;
import org.griphyn.vdl.karajan.FutureTracker;
import org.griphyn.vdl.type.Field;

public class ArrayDataNode extends DataNode {
	protected ArrayDataNode(Field field, DSHandle root, DSHandle parent) {
		super(field, root, parent);
	}
	
	public void getFringePaths(List<Path> list, Path parentPath) throws HandleOpenException {
		checkMappingException();
		if (!isClosed()) {
		    throw new FutureNotYetAvailable(getFutureWrapper());
		}
		Map<Comparable<?>, DSHandle> handles = getHandles();
		synchronized (handles) {
			for (Map.Entry<Comparable<?>, DSHandle> e : handles.entrySet()) {
				AbstractDataNode mapper = (AbstractDataNode) e.getValue();
				Path fullPath = parentPath.addLast(e.getKey().toString(), getType().isArray());
				if (!mapper.isHandlesEmpty()) {
					mapper.getFringePaths(list, fullPath);
				}
				else if (!mapper.getField().getType().isPrimitive()) {
					list.add(fullPath);
				}
			}
		}
	}
	
	/** Recursively closes arrays through a tree of arrays and complex
        types. */
    public void closeDeep() {
        assert(this.getType().isArray());
        closeShallow();
        Map<Comparable<?>, DSHandle> handles = getHandles();
        synchronized (handles) {
        	for (Map.Entry<Comparable<?>, DSHandle> e : handles.entrySet()) {
                AbstractDataNode child = (AbstractDataNode) e.getValue();
                child.closeDeep();
            }
        }
    }

	
	public boolean isArray() {
		return true;
	}
	
	public int size() {
		return getHandles().size();
	}
	
    @Override
    protected void setField(String name, DSHandle handle) {
        super.setField(name, handle);
        addKey(name);
    }
    
    private void addKey(String name) {
        ArrayIndexFutureList w;
        synchronized(this) {
            w = (ArrayIndexFutureList) wrapper;
        }
        if (w != null) {
            w.addKey(name);
        }
    }

    @Override
    public DSHandle createDSHandle(String fieldName)
            throws NoSuchFieldException {
        DSHandle h = super.createDSHandle(fieldName);
        addKey(fieldName);
        return h;
    }

    @Override
    protected synchronized Future getFutureWrapper() {
    	if (wrapper == null) {
    		wrapper = new ArrayIndexFutureList(this, this.getArrayValue());
    		FutureTracker.get().add(this, wrapper);
    	}
        return wrapper;
    }

    public FutureList getFutureList() {
        return (FutureList) getFutureWrapper();
    }
}
