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

	public String toString() {
		String prefix = getDisplayableName();
		//return getType() + " " + prefix + "." + getPathFromRoot() + "[]/" + getHandles().size()
			//	+ ": " + getValue();
		return prefix + "." + getPathFromRoot() + "[]/" + getHandles().size();
	}
	
	public void getFringePaths(List list, Path parentPath) throws HandleOpenException {
		checkMappingException();
		if (!isClosed()) {
			throw new HandleOpenException(this);
		}
		Map handles = getHandles();
		synchronized (handles) {
			Iterator i = handles.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				AbstractDataNode mapper = (AbstractDataNode) e.getValue();
				// Why would I do this?
				// Path fullPath = parentPath.addLast(mapper.fieldName);
				Path fullPath = parentPath.addLast((String) e.getKey());
				if (!mapper.isHandlesEmpty()) {
					mapper.getFringePaths(list, fullPath);
				}
				else if (!mapper.getField().getType().isPrimitive()) {
					list.add(fullPath.toString());
				}
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
