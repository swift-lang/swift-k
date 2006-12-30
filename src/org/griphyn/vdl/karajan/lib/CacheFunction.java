/*
 * Created on Dec 28, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.PartialArgumentsContainer;
import org.griphyn.vdl.karajan.functions.ConfigProperty;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCache;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCacheFactory;
import org.griphyn.vdl.util.VDL2ConfigProperties;

public abstract class CacheFunction extends PartialArgumentsContainer {
	public static final String CACHE_FILES_TO_REMOVE = "cachefilestoremove";

	public static final String VDL_FILE_CACHE = "vdl:filecache";

	protected static VDLFileCache getCache(VariableStack stack) throws ExecutionException {
		VDLFileCache cache;
		synchronized (stack.getExecutionContext()) {
			cache = (VDLFileCache) stack.getGlobal(VDL_FILE_CACHE);
			if (cache == null) {
				cache = VDLFileCacheFactory.newInstance(ConfigProperty.getProperty(
						VDL2ConfigProperties.CACHING_ALGORITHM, stack));
				stack.setGlobal(VDL_FILE_CACHE, cache);
			}
		}
		return cache;
	}

	public static class CacheEntry {
		public String fullPath;
		public Object host;

		public CacheEntry(String fullPath, Object host) {
			this.fullPath = fullPath;
			this.host = host;
		}

		public boolean equals(Object other) {
			if (other instanceof CacheEntry) {
				CacheEntry ce = (CacheEntry) other;
				return fullPath.equals(ce.fullPath) && host.equals(ce.host);
			}
			return false;
		}

		public int hashCode() {
			return fullPath.hashCode() + host.hashCode();
		}
	}
}
