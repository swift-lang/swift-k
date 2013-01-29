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


package org.griphyn.vdl.karajan.lib;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;

/** Determines if a variable is 'restartable'; that is, if we restart the
    workflow, will this variable still have its content.
*/
    
    

public class IsRestartable extends SwiftFunction {
    private ArgRef<DSHandle> var;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("var"));
    }

    @Override
    public Object function(Stack stack) {
        DSHandle var = this.var.getValue(stack);
        return Boolean.valueOf(var.isRestartable());
    }
}

