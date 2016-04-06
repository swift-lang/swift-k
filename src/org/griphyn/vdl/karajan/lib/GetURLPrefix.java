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

import k.rt.Context;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.griphyn.vdl.karajan.SwiftContext;
import org.griphyn.vdl.util.SwiftConfig;

public class GetURLPrefix extends AbstractSingleValuedFunction {
    private VarRef<Context> context;
    
    @Override
    protected Param[] getParams() {
        return params();
    }

    @Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        context = scope.getVarRef("#context");
    }

    @Override
    public Object function(Stack stack) {
        Context ctx = this.context.getValue(stack);
        SwiftConfig config = (SwiftConfig) ctx.getAttribute(SwiftContext.ATTR_SWIFT_CONFIG);
        String localServerBase = config.getWrapperStagingLocalServer();
        String cwd = this.context.getValue(stack).getCWD();
        
        if (cwd.endsWith("/.")) {
            cwd = cwd.substring(0, cwd.length() - 2);
        }
        
        return localServerBase + cwd;
    }
}
