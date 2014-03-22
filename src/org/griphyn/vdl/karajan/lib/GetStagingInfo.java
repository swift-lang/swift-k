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
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.type.Type;

public class GetStagingInfo extends SwiftFunction {
	
    private ArgRef<List<DSHandle>> stageins;
	private ArgRef<List<DSHandle>> stageouts;
    private ChannelRef<Object> cr_vargs;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("stageins", "stageouts"), returns(channel("...", 4)));
    }


    @Override
    public Object function(Stack stack) {
        Collection<DSHandle> fi = stageins.getValue(stack);
        Collection<DSHandle> fo = stageouts.getValue(stack);
        Channel<Object> ret = cr_vargs.get(stack);
        Set<String> localDirNames = new HashSet<String>();
        Set<String> remoteDirNames = new HashSet<String>();
        List<AbsFile> inFiles = new ArrayList<AbsFile>();
        List<AbsFile> outFiles = new ArrayList<AbsFile>();
        
        try {
            addPaths(localDirNames, remoteDirNames, inFiles, fi);
            addPaths(localDirNames, remoteDirNames, outFiles, fo);
        }
        catch (HandleOpenException e) {
        	throw new ExecutionException(e.getMessage(), e);
        }
        ret.add(localDirNames);
        ret.add(remoteDirNames);
        ret.add(inFiles);
        ret.add(outFiles);
        return null;
    }

    private void addPaths(Set<String> ldirs, Set<String> rdirs, List<AbsFile> files, Collection<DSHandle> vars) throws HandleOpenException {
    	for (DSHandle file : vars) {
            for (DSHandle leaf : file.getLeaves()) {
            	Type t = leaf.getType();
                String fname = SwiftFunction.argList(SwiftFunction.filename(leaf), true);
                AbsFile f = new AbsFile(fname);
                String dir = f.getDirectory();
                if (dir != null) {
                    ldirs.add(dir);
                    rdirs.add(remoteDir(f, dir));
                }
                files.add(f);
            }
        }
    }


    private String remoteDir(AbsFile f, String dir) {
        if ("file".equals(f.getProtocol())) {
            return PathUtils.remotePathName(dir);
        }
        else {
            // also prepend host name to the path
            return f.getHost() + "/" + PathUtils.remotePathName(dir);
        }
    }
}
