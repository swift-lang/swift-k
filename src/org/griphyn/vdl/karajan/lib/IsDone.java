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

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.restartLog.LogChannelOperator;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class IsDone extends SwiftFunction {
    private ArgRef<Iterable<List<Object>>> stageout;
    
    private ChannelRef<String> cr_restartLog;

    @Override
    protected Signature getSignature() {
        return new Signature(params("stageout"), returns(channel("...", 1), channel("restartLog")));
    }
    
    @Override
    public Object function(Stack stack) {
        Iterable<List<Object>> files = stageout.getValue(stack);
        for (List<Object> pv : files) {
            Path p = (Path) pv.get(0);
            DSHandle handle = (DSHandle) pv.get(1);
            if (!IsLogged.isLogged((LogChannelOperator) cr_restartLog.get(stack), handle, p)) {
                return Boolean.FALSE;
            }
        }
        if (!files.iterator().hasNext()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
