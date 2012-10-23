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

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class AppendArray extends SetFieldValue {
    
    public static final Arg PA_ID = new Arg.Positional("id");

    static {
        setArguments(AppendArray.class, new Arg[] { PA_VAR, PA_VALUE });
    }
    
    @Override
    public Object function(VariableStack stack) throws ExecutionException {
        DSHandle var = (DSHandle) PA_VAR.getValue(stack);
        AbstractDataNode value = (AbstractDataNode) PA_VALUE.getValue(stack);
        // while there isn't a way to avoid conflicts between auto generated indices
        // and a user manually using the same index, adding a "#" may reduce
        // the incidence of problems
        Path path = Path.EMPTY_PATH.addFirst(getThreadPrefix(stack), true);
        try {
            deepCopy(var.getField(path), value, stack, 0);
        }
        catch (InvalidPathException e) {
            throw new ExecutionException(e);
        }
        return null;
    }
}
