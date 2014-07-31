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
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.lib.cache.File;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCache;

public class CacheFileRemoved extends CacheFunction {
	private ArgRef<String> path;
	private ArgRef<Object> host;
	
	@Override
    protected Signature getSignature() {
        return new Signature(params("path", "host"));
    }

	
	@Override
    protected void runBody(LWThread thr) {
        Stack stack = thr.getStack();
        String path = this.path.getValue(stack);
        Object host = this.host.getValue(stack);
        VDLFileCache cache = getCache(stack);
        File f = new File(path, host, 0);
        cache.entryRemoved(f);
    }
}
