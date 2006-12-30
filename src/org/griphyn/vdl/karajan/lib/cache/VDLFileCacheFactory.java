/*
 * Created on Dec 28, 2006
 */
package org.griphyn.vdl.karajan.lib.cache;

public class VDLFileCacheFactory {

	public static VDLFileCache newInstance(String type) {
		if ("LRU".equals(type)) {
			return new LRUFileCache();
		}
		else {
			throw new IllegalArgumentException("No such cache algorithm: " + type);
		}
	}
}
