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

import k.rt.Context;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCache;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCacheFactory;
import org.griphyn.vdl.util.SwiftConfig;

public abstract class CacheFunction extends InternalFunction {
	public static final String CACHE_FILES_TO_REMOVE = "cacheFilesToRemove";

	private VarRef<Context> context;
	private VDLFileCache cache;
	
    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        context = scope.getVarRef("#context");
        return super.compileBody(w, argScope, scope);
    }

    protected VDLFileCache getCache(Stack stack) {
        synchronized(this) {
            if (cache == null) {
                cache = getOrCreateCache(stack);
            }
            return cache;
        }
	}

    private VDLFileCache getOrCreateCache(Stack stack) {
        Context ctx = context.getValue(stack);
        synchronized(ctx) {
            VDLFileCache cache = (VDLFileCache) ctx.getAttribute("SWIFT:FILE_CACHE");
            if (cache == null) {
                SwiftConfig conf = (SwiftConfig) ctx.getAttribute("SWIFT:CONFIG");
                cache = VDLFileCacheFactory.newInstance(conf.getCachingAlgorithm());
                ctx.setAttribute("SWIFT:FILE_CACHE", cache);
            }
            return cache;
        }
    }
}
