/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
}
