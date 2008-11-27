/*
 * Created on Jul 6, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.griphyn.vdl.mapping.DSHandle;

public class WrapperMap {
	private Map map;

	public WrapperMap() {
		this.map = new HashMap();
	}

	public static class FutureWrappers {
		public DSHandleFutureWrapper nodeWrapper;
		public ArrayIndexFutureList arrayWrapper;
	}

	public synchronized void close(DSHandle handle) {
		FutureWrappers fw = (FutureWrappers) map.get(handle);
		if (fw != null) {
			if (fw.nodeWrapper != null) {
				fw.nodeWrapper.close();
			}
			if (fw.arrayWrapper != null) {
				fw.arrayWrapper.close();
			}
		}
	}

	public synchronized boolean isClosed(DSHandle handle) {
		FutureWrappers fw = (FutureWrappers) map.get(handle);
		if (fw != null) {
			if (fw.nodeWrapper != null) {
				return fw.nodeWrapper.isClosed();
			}
			else if (fw.arrayWrapper != null) {
				return fw.arrayWrapper.isClosed();
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	public synchronized DSHandleFutureWrapper addNodeListener(DSHandle handle) {
		FutureWrappers fw = (FutureWrappers) map.get(handle);
		if (fw == null) {
			map.put(handle, fw = new FutureWrappers());
		}
		if (fw.nodeWrapper == null) {
			fw.nodeWrapper = new DSHandleFutureWrapper(handle);
		}
		return fw.nodeWrapper;
	}

	public synchronized ArrayIndexFutureList addFutureListListener(DSHandle handle, Map value) {
		FutureWrappers fw = (FutureWrappers) map.get(handle);
		if (fw == null) {
			map.put(handle, fw = new FutureWrappers());
		}
		if (fw.arrayWrapper == null) {
			fw.arrayWrapper = new ArrayIndexFutureList(handle, value);
		}
		return fw.arrayWrapper;
	}

	public synchronized void mergeListeners(DSHandle destination, DSHandle source) {
		FutureWrappers fwd = (FutureWrappers) map.get(destination);
		FutureWrappers fws = (FutureWrappers) map.get(source);

		// TODO
		throw new RuntimeException("not implemented");
	}

	public synchronized void markAsAvailable(DSHandle handle, Object key) {
		FutureWrappers fw = (FutureWrappers) map.get(handle);
		if (fw != null && fw.arrayWrapper != null) {
			fw.arrayWrapper.addKey(key);
		}
	}

	public Set entrySet() {
		return map.entrySet();
	}
}
