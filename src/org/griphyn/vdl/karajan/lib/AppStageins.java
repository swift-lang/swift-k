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

import java.util.LinkedList;
import java.util.List;

import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.swift.data.Director;
import org.globus.swift.data.policy.Policy;
import org.griphyn.vdl.mapping.AbsFile;

public class AppStageins extends InternalFunction {
	private ArgRef<String> jobid;
	private ArgRef<List<AbsFile>> files;
	
	private ChannelRef<List<String>> cr_stagein;
	
	private VarRef<String> cwd;

    static Logger logger = Logger.getLogger(AppStageins.class);
    
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("jobid", "files"), returns(channel("stagein")));
    }
    
    @Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        cwd = scope.getVarRef("CWD");
    }

    
    protected void runBody(LWThread thr) {
    	Stack stack = thr.getStack();
    	List<AbsFile> files = this.files.getValue(stack);
        String cwd = this.cwd.getValue(stack);
        for (AbsFile file : files) {
            Policy policy = Director.lookup(file.toString());
            if (policy != Policy.DEFAULT) {
                logger.debug("will not stage in (CDM): " + file);
                continue; 
            }
                                        
            String protocol = file.getProtocol();
            if ("direct".equals(protocol)) {
                continue;
            }
            String path = file.getDirectory() == null ? 
                    file.getName() : file.getDirectory() + "/" + file.getName();
            String relpath = PathUtils.remotePathName(path);
            if (logger.isDebugEnabled()) {
                logger.debug("will stage in: " + relpath + " via: " + protocol);
            }
            cr_stagein.append(stack,
                makeList(localPath(cwd, protocol, path, file), relpath));
        }
    }

    protected static String localPath(String cwd, String protocol, String path, AbsFile file) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol);
        sb.append("://");
        sb.append(file.getHost());
        sb.append('/');
        if (!file.isAbsolute()) {
            sb.append(cwd);
            sb.append('/');
        }
        sb.append(path);
        return sb.toString();
    }

    private List<String> makeList(String s1, String s2) {
        List<String> l = new LinkedList<String>();
        l.add(s1);
        l.add(s2);
        return l;
    }
}
