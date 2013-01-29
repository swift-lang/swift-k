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
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.swift.data.Director;
import org.globus.swift.data.policy.Policy;
import org.griphyn.vdl.mapping.AbsFile;

public class AppStageins extends InternalFunction {
	private ArgRef<String> jobid;
	private ArgRef<List<String>> files;
	private ArgRef<String> dir;
	private ArgRef<String> stagingMethod;
	
	private ChannelRef<List<String>> cr_stagein;

    static Logger logger = Logger.getLogger(AppStageins.class);
    
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("jobid", "files", "dir", "stagingMethod"), returns(channel("stagein")));
    }

    
    protected void runBody(LWThread thr) {
    	Stack stack = thr.getStack();
    	List<String> files = this.files.getValue(stack);
    	String stagingMethod = this.stagingMethod.getValue(stack);
    	String dir = this.dir.getValue(stack);
        for (Object f : files) {
            AbsFile file = new AbsFile(TypeUtil.toString(f));
            Policy policy = Director.lookup(file.toString());
            if (policy != Policy.DEFAULT) {
                logger.debug("will not stage in (CDM): " + file);
                continue; 
            }
                                        
            String protocol = file.getProtocol();
            if (protocol.equals("file")) {
                protocol = stagingMethod;
            }
            String path = file.getDir().equals("") ? file.getName() : file
                .getDir()
                    + "/" + file.getName();
            String relpath = path.startsWith("/") ? path.substring(1) : path;
            if (logger.isDebugEnabled()) {
                logger.debug("will stage in: " + relpath + " via: " + protocol);
            }
            cr_stagein.append(stack,
                makeList(protocol + "://" + file.getHost() + "/" + path,
                    dir + "/" + relpath));
        }
    }

    private List<String> makeList(String s1, String s2) {
        List<String> l = new LinkedList<String>();
        l.add(s1);
        l.add(s2);
        return l;
    }
}
