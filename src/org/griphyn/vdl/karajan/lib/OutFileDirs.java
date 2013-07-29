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
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class OutFileDirs extends SwiftFunction {
	
	private ArgRef<List<List<Object>>> stageouts;
    private ChannelRef<Object> cr_vargs;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("stageouts"), returns(channel("...", DYNAMIC)));
    }


    @Override
    public Object function(Stack stack) {
        List<List<Object>> files = stageouts.getValue(stack);
        Channel<Object> ret = cr_vargs.get(stack);
        try {
            for (List<Object> pv : files) {
                Path p = parsePath(pv.get(0));
                DSHandle handle = (DSHandle) pv.get(1);
                DSHandle leaf = handle.getField(p);
                String fname = SwiftFunction.filename(leaf)[0];
                AbsFile af = new AbsFile(fname);
                String dir = af.getDirectory();
                if (dir != null) {
                    if ("file".equals(af.getProtocol())) {
                        ret.add(PathUtils.remotePathName(dir));
                    }
                    else {
                        ret.add(af.getHost() + "/" + PathUtils.remotePathName(dir));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new ExecutionException(this, e);
        }
        return null;
    }
}
