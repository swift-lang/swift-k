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
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.List;

import k.rt.Context;
import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.swift.data.Director;
import org.globus.swift.data.policy.Policy;
import org.griphyn.vdl.karajan.SwiftContext;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.util.RootFS;

public class AppStageins extends AppStageFiles {
	private ArgRef<String> jobid;
	private ArgRef<List<AbsFile>> files;
	
	private ChannelRef<List<String>> cr_stagein;
	
	private VarRef<SwiftContext> ctx;
	
    static Logger logger = Logger.getLogger(AppStageins.class);
    
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("jobid", "files"), returns(channel("stagein")));
    }
    
    @Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        ctx = scope.getVarRef(Context.VAR_NAME);
    }

    
    protected void runBody(LWThread thr) {
    	Stack stack = thr.getStack();
    	List<AbsFile> files = this.files.getValue(stack);
        RootFS rfs = this.ctx.getValue(stack).getRootFS();
        CacheKeyTmp key = new CacheKeyTmp();
        for (AbsFile file : files) {
            Policy policy = Director.lookup(file.toString());
            if (policy != Policy.DEFAULT) {
                logger.debug("will not stage in (CDM): " + file);
                continue; 
            }
            
            key.set(file);
            List<String> cached = getFromCache(key);
            
            if (cached != null) {
                cr_stagein.append(stack, cached);
            }
            else {                               
                String protocol = file.getProtocol();
                if ("direct".equals(protocol)) {
                    continue;
                }
                String relpath = PathUtils.remotePathName(file);
                if (logger.isDebugEnabled()) {
                    logger.debug("will stage in: " + relpath + " via: " + protocol);
                }
                List<String> value = makeList(file.toString(), relpath);
                putInCache(key, value);
                cr_stagein.append(stack, value);
            }
        }
    }
}
