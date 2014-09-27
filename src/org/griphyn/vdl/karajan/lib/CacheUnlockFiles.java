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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.griphyn.vdl.mapping.AbsFile;

public class CacheUnlockFiles extends CacheFunction {
    private ArgRef<Collection<AbsFile>> files;
    private ArgRef<String> dir;
    private ArgRef<BoundContact> host;
    private ArgRef<Boolean> force;
    private Node body;
    
    private VarRef<List<?>> cacheFilesToRemove;

    @Override
    protected Signature getSignature() {
        return new Signature(params("files", "dir", "host", optional("force", Boolean.TRUE), block("body")));
    }
    
	@Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        cacheFilesToRemove = scope.getVarRef(scope.addVar(CACHE_FILES_TO_REMOVE));
    }

    @Override
    protected void runBody(LWThread thr) {
        int i = thr.checkSliceAndPopState();
        try {
            switch (i) {
                case 0:
                    remove(thr.getStack());
                    i++;
                case 1:
                    body.run(thr);
            }
        }
        catch (Yield y) {
            y.getState().push(i);
            throw y;
        }
    }

    public void remove(Stack stack) {
        Collection<AbsFile> files = this.files.getValue(stack);
        String dir = this.dir.getValue(stack);
        Object host = this.host.getValue(stack);
        VDLFileCache cache = getCache(stack);
        List<Object> rem = new ArrayList<Object>();
        
        boolean force = this.force.getValue(stack);
        
        for (AbsFile f : files) {
            File cf = new File(f.getName(), dircat(dir, PathUtils.remoteDirName(f)), host, 0);
            CacheReturn cr = cache.unlockEntry(cf, force);
            rem.addAll(cr.remove);
        }
        
        cacheFilesToRemove.setValue(stack, rem);
    }

    private String dircat(String dir1, String dir2) {
        if (dir2 == null || "".equals(dir2)) {
            return dir1;
        }
        else {
            return dir1 + "/" + dir2;
        }
    }
}
