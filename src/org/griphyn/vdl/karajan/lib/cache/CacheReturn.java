/*
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib.cache;

import java.util.List;

public class CacheReturn {
	public final boolean alreadyCached;
	public final List remove;
	public final File cached;
	
	public CacheReturn(boolean alreadyCached, List remove, File cached) {
		this.alreadyCached = alreadyCached;
		this.remove = remove;
		this.cached = cached;
	}
}
