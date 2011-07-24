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
				Path fullPath = parentPath.addLast(e.getKey(), getType().isArray());
				if (mapper.getType().isComposite()) {
					mapper.getFringePaths(list, fullPath);
				}
				else if (!mapper.getType().isPrimitive()) {
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
    protected void setField(Comparable<?> id, DSHandle handle) {
        synchronized(getHandles()) {
            super.setField(id, handle);
            // Operations on the handles and the wrapper keys need to be synchronized.
            // When a wrapper is created, it populates its list of keys with the handles
            // available. If this happens concurrently with a call being exactly at this
            // point in the code, then it's possible for a key to have both been added
            // as part of the creation of the wrapper as well as due to the next call,
            // causing duplicate iterations.
            addKey(id);
        }
    }
    
    private void addKey(Comparable<?> key) {
        if (wrapper != null) {
            ((ArrayIndexFutureList) wrapper).addKey(key);
        }
    }
    
    @Override
    public DSHandle createField(Comparable<?> key) throws NoSuchFieldException {
        synchronized(getHandles()) {
            DSHandle h = super.createField(key);
            addKey(key);
            return h;
        }
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
    
    protected void getFields(List<DSHandle> fields, Path path) throws InvalidPathException {
        if (path.isEmpty()) {
            fields.add(this);
        }
        else {
            Path rest = path.butFirst();
            if (path.isWildcard(0)) {
                if (!isClosed()) {
                    throw new FutureNotYetAvailable(getFutureWrapper());
                }
                for (DSHandle handle : getHandles().values()) {
                    ((AbstractDataNode) handle).getFields(fields, rest);
                }
            }
            else {
                try {
                    ((AbstractDataNode) getField(path.getKey(0))).getFields(
                        fields, path.butFirst());
                }
                catch (NoSuchFieldException e) {
                    throw new InvalidPathException(path, this);
                }
            }
        }
    }
}
