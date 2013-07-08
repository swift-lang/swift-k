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

import java.util.List;

import k.rt.Channel;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.griphyn.vdl.mapping.AbsFile;

public class InFileDirs extends InternalFunction {
    
    private ArgRef<List<String>> stageins;
    private ChannelRef<Object> cr_vargs;
   
    @Override
    protected Signature getSignature() {
        return new Signature(params("stageins"), returns(channel("...", DYNAMIC)));
    }


    @Override
    protected void runBody(LWThread thr) {
        Stack stack = thr.getStack();
        List<String> files = stageins.getValue(stack);
        Channel<Object> ret = cr_vargs.get(stack);
        for (String path : files) {
        	AbsFile af = new AbsFile(path);
        	String dir = af.getDir();
        	if ("file".equals(af.getProtocol())) {
                ret.add(PathUtils.remotePathName(dir));
        	}
        	else {
        	    // also prepend host name to the path
        	    ret.add(af.getHost() + "/" + PathUtils.remotePathName(dir));
        	}
        }
    }
}
