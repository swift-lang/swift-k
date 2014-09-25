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

import java.util.Collection;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.AbsFile;

public class RemoteFileNames extends SwiftFunction {
    private ArgRef<Collection<Object>> files;
        
    @Override
    protected Signature getSignature() {
        return new Signature(params("files"));
    }

    @Override
    public Object function(Stack stack) {
        Collection<Object> files = this.files.getValue(stack);
        StringBuilder sb = new StringBuilder();
        for (Object o : files) {
            if (sb.length() > 0) {
                sb.append('|');
            }
            if (o instanceof String) {
                sb.append(PathUtils.remotePathName((String) o));
            }
            else {
                AbsFile f = (AbsFile) o;
                sb.append(PathUtils.remotePathName(f));
            }
        }
        return sb.toString();
    }
}
