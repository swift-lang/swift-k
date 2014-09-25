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
import java.util.Collections;
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
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class GetStagingInfo extends SwiftFunction {
	
    private ArgRef<List<DSHandle>> stageins;
	private ArgRef<List<DSHandle>> stageouts;
	private ArgRef<String> defaultScheme;
    private ChannelRef<Object> cr_vargs;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("stageins", "stageouts", optional("defaultScheme", null)), 
            returns(channel("...", 5)));
    }
    
    private static class Info {
        Set<AbsFile> remoteDirNames = Collections.emptySet();
        Set<AbsFile> inFiles = Collections.emptySet();
        Set<AbsFile> outFiles = Collections.emptySet();
        Set<AbsFile> collectPatterns = Collections.emptySet();
    }

    @Override
    public Object function(Stack stack) {
        Collection<DSHandle> fi = stageins.getValue(stack);
        Collection<DSHandle> fo = stageouts.getValue(stack);
        String defaultScheme = this.defaultScheme.getValue(stack);
        Channel<Object> ret = cr_vargs.get(stack);
        if (defaultScheme == null) {
            defaultScheme = "file";
        }
        
        Info info = new Info();
        
        try {
            addPaths(info, fi, false, defaultScheme);
            addPaths(info, fo, true, defaultScheme);
        }
        catch (HandleOpenException e) {
        	throw new ExecutionException(e.getMessage(), e);
        }
        ret.add(new ArrayList<AbsFile>(info.remoteDirNames));
        ret.add(new ArrayList<AbsFile>(info.inFiles));
        ret.add(new ArrayList<AbsFile>(info.outFiles));
        ret.add(new ArrayList<AbsFile>(info.collectPatterns));
        return null;
    }

    private void addPaths(Info info, Collection<DSHandle> vars, boolean out, String defaultScheme) throws HandleOpenException {
    	for (DSHandle var : vars) {
    	    if (!var.getType().hasMappedComponents()) {
    	        continue;
    	    }
    	    Mapper m = getMapper(var);
    	    if (out && !m.isStatic() && var.getType().hasArrayComponents()) {
    	        Collection<AbsFile> patterns = m.getPattern(var.getPathFromRoot(), var.getType());
    	        for (AbsFile f : patterns) {
    	            info.collectPatterns = addOne(f, info, info.collectPatterns, defaultScheme);
    	        }
    	    }
    	    else {
    	        addAllStatic(var, m, info, out, defaultScheme);
    	    }    
        }
    }


    private void addAllStatic(DSHandle var, Mapper m, Info info, boolean out, String defaultScheme) throws HandleOpenException {
        for (DSHandle leaf : var.getLeaves()) {
            Type t = leaf.getType();
            if (t.equals(Types.EXTERNAL)) {
                continue;
            }
            if (out) {
                info.outFiles = addOne((AbsFile) m.map(leaf.getPathFromRoot()), info, info.outFiles, defaultScheme);
            }
            else {
                info.inFiles = addOne((AbsFile) m.map(leaf.getPathFromRoot()), info, info.inFiles, defaultScheme);
            }
        }
    }


    private Set<AbsFile> addOne(AbsFile f, Info info, Set<AbsFile> files, String defaultScheme) {
        String proto = f.getProtocol();
        if (proto == null) {
            f.setProtocol(defaultScheme);
        }
        if (f.getHost() == null) {
            f.setHost("localhost");
        }
        
        String dir = f.getDirectory();
        if (info.remoteDirNames.isEmpty()) {
            info.remoteDirNames = new HashSet<AbsFile>();
        }
        if (dir == null) {
            info.remoteDirNames.add(new AbsFile(f.getProtocol(), f.getHost(), "."));
        }
        else {
            info.remoteDirNames.add(new AbsFile(f.getProtocol(), f.getHost(), f.getDirectory()));
        }
        if (files.isEmpty()) {
            files = new HashSet<AbsFile>();
        }
        files.add(f);
        return files;
    }

    private Mapper getMapper(DSHandle var) {
        Mapper m = var.getMapper();
        if (m == null) {
            throw new ExecutionException("No mapper found for  " + var);
        }
        return m;
    }
}
