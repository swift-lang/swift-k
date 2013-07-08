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

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.griphyn.vdl.karajan.Pair;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class AppStageouts extends InternalFunction {
    private ArgRef<String> jobid;
    private ArgRef<List<List<Object>>> files;
    private ArgRef<String> stagingMethod;
    
    private ChannelRef<List<String>> cr_stageout;
    
    private VarRef<String> cwd;

    
    @Override
    protected Signature getSignature() {
        return new Signature(params("jobid", "files", "stagingMethod"), returns(channel("stageout")));
    }
    
    @Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        cwd = scope.getVarRef("CWD");
    }

    protected void runBody(LWThread thr) {
        try {
            Stack stack = thr.getStack();
            List<List<Object>> files = this.files.getValue(stack);
            String stagingMethod = this.stagingMethod.getValue(stack);
            String cwd = this.cwd.getValue(stack);

            for (List<Object> pv : files) { 
                Path p = (Path) pv.get(0);
                DSHandle handle = (DSHandle) pv.get(1);
                AbsFile file = new AbsFile(SwiftFunction.filename(handle.getField(p))[0]);
                String protocol = file.getProtocol();
                if (protocol.equals("file")) {
                    protocol = stagingMethod;
                }
                String path = file.getDir().equals("") ? file.getName() : file.getDir()
                        + "/" + file.getName();
                String relpath = path.startsWith("/") ? path.substring(1) : path;
                cr_stageout.append(stack, 
                    makeList(relpath,
                        AppStageins.localPath(cwd, protocol, path, file)));
            }
        }
        catch (Exception e) {
            throw new ExecutionException(this, e);
        }
    }
    
    private List<String> makeList(String s1, String s2) {
        return new Pair<String>(s1, s2);
    }
}
