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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.AbsFile;

public class RemoteFileNames extends SwiftFunction {
    private ArgRef<Collection<AbsFile>> files;
        
    @Override
    protected Signature getSignature() {
        return new Signature(params("files"));
    }

    @Override
    public Object function(Stack stack) {
        Collection<AbsFile> files = this.files.getValue(stack);
        List<String> ret = new ArrayList<String>();
        for (AbsFile f : files) {
            if ("file".equals(f.getProtocol())) {
                ret.add(PathUtils.remotePathName(f.getPath()));
            }
            else {
                ret.add(PathUtils.remotePathName(f.getHost() + "/" + f.getPath()));
            }
        }
        return ret;
    }
}
