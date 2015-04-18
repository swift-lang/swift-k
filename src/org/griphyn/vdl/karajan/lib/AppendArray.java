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

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

public class AppendArray extends SetFieldValue {
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("var", "value"));
    }

    @Override
    public Object function(Stack stack) {
        DSHandle var = this.var.getValue(stack);
        AbstractDataNode value = this.value.getValue(stack);
        // while there isn't a way to avoid conflicts between auto generated indices
        // and a user manually using the same index, adding a "#" may reduce
        // the incidence of problems
        try {
            deepCopy(var.getField(getThreadPrefix()), value, stack);
        }
        catch (NoSuchFieldException e) {
            throw new ExecutionException(this, e);
        }
        catch (InvalidPathException e) {
            throw new ExecutionException(this, e);
        }
        return null;
    }
}
