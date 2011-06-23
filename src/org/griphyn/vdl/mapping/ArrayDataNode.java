/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.griphyn.vdl.type.Field;

public class ArrayDataNode extends DataNode {
	protected ArrayDataNode(Field field, DSHandle root, DSHandle parent) {
		super(field, root, parent);
	}
	
	public void getFringePaths(List list, Path parentPath) throws HandleOpenException {
		checkMappingException();
		if (!isClosed()) {
			throw new HandleOpenException(this);
		}
		Map<String,DSHandle> handles = getHandles();
		synchronized (handles) {
			Iterator i = handles.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				AbstractDataNode mapper = (AbstractDataNode) e.getValue();
				Path fullPath = parentPath.addLast((String) e.getKey(), getType().isArray());
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
        if (!this.isClosed()) {
            closeShallow();
        }
        Map handles = getHandles();
        synchronized (handles) {
            Iterator i = handles.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
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
}
