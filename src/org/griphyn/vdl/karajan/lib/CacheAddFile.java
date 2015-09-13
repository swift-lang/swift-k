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

import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.util.BoundContact;
import org.griphyn.vdl.karajan.lib.cache.CacheReturn;
import org.griphyn.vdl.karajan.lib.cache.File;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCache;

public class CacheAddFile extends CacheFunction {
	private ArgRef<String> file;
    private ArgRef<String> dir;
    private ArgRef<BoundContact> host;
    private ArgRef<Number> size;
    private Node body;
    
    private VarRef<List<?>> cacheFilesToRemove;

	@Override
    protected Signature getSignature() {
        return new Signature(params("file", "dir", "host", "size", block("body")));
    }
	
	@Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        cacheFilesToRemove = scope.getVarRef(scope.addVar(CACHE_FILES_TO_REMOVE));
    }

    @Override
    protected void runBody(LWThread thr) {
        int i = thr.checkSliceAndPopState(1);
        try {
            switch (i) {
                case 0:
                    add(thr.getStack());
                    i++;
                default:
                    body.run(thr);
            }
        }
        catch (Yield y) {
            y.getState().push(i, 1);
            throw y;
        }
    }

    public void add(Stack stack) {
    	String file = this.file.getValue(stack);
        String dir = this.dir.getValue(stack);
        BoundContact host = this.host.getValue(stack);
        long size = this.size.getValue(stack).longValue();
        VDLFileCache cache = getCache(stack);
		File f = new File(file, dir, host, size);
		CacheReturn cr = cache.addEntry(f);
		if (cr.alreadyCached) {
			throw new ExecutionException(this, "The cache already contains " + f + ".");
		}
		cacheFilesToRemove.setValue(stack, cr.remove);
	}
}
