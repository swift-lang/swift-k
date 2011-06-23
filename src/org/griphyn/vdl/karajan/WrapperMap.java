/*
 * Created on Jul 6, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.griphyn.vdl.mapping.DSHandle;

public class WrapperMap {
	private Map<DSHandle, FutureWrappers> map;

	public WrapperMap() {
		this.map = new HashMap<DSHandle, FutureWrappers>();
	}

	public static class FutureWrappers {
		public DSHandleFutureWrapper nodeWrapper;
		public ArrayIndexFutureList arrayWrapper;
	}

	public void close(DSHandle handle) {
	    DSHandleFutureWrapper nodeWrapper;
	    ArrayIndexFutureList arrayWrapper;
	    synchronized(this) {
	        FutureWrappers fw = map.get(handle);
	        if (fw == null) {
	            return;
	        }
	        nodeWrapper = fw.nodeWrapper;
	        arrayWrapper = fw.arrayWrapper;
	    }
		if (nodeWrapper != null) {
			nodeWrapper.close();
		}
		if (arrayWrapper != null) {
			arrayWrapper.close();
		}
	}

	public boolean isClosed(DSHandle handle) {
	    DSHandleFutureWrapper nodeWrapper;
        ArrayIndexFutureList arrayWrapper;
        synchronized(this) {
            FutureWrappers fw = map.get(handle);
            if (fw == null) {
                return false;
            }
            nodeWrapper = fw.nodeWrapper;
            arrayWrapper = fw.arrayWrapper;
        }
		if (nodeWrapper != null) {
			return nodeWrapper.isClosed();
		}
		else if (arrayWrapper != null) {
			return arrayWrapper.isClosed();
		}
		else {
			return false;
		}
	}

	public synchronized DSHandleFutureWrapper addNodeListener(DSHandle handle) {
		FutureWrappers fw = map.get(handle);
		if (fw == null) {
			map.put(handle, fw = new FutureWrappers());
		}
		if (fw.nodeWrapper == null) {
			assert Thread.holdsLock(handle.getRoot()); // TODO should be on root or on handle?
			fw.nodeWrapper = new DSHandleFutureWrapper(handle);
		}
		return fw.nodeWrapper;
	}

	public synchronized ArrayIndexFutureList addFutureListListener(DSHandle handle, Map<?, ?> value) {
		FutureWrappers fw = map.get(handle);
		if (fw == null) {
			map.put(handle, fw = new FutureWrappers());
		}
		if (fw.arrayWrapper == null) {
			assert Thread.holdsLock(handle.getRoot()); // TODO should be on root or on handle?
			fw.arrayWrapper = new ArrayIndexFutureList(handle, value);
		}
		return fw.arrayWrapper;
	}

	public synchronized void mergeListeners(DSHandle destination, DSHandle source) {
		// TODO
		throw new RuntimeException("not implemented");
	}

	public synchronized void markAsAvailable(DSHandle handle, Object key) {
		FutureWrappers fw = map.get(handle);
		if (fw != null && fw.arrayWrapper != null) {
			fw.arrayWrapper.addKey(key);
		}
	}

	public Set<Map.Entry<DSHandle, FutureWrappers>> entrySet() {
		return map.entrySet();
	}
}
