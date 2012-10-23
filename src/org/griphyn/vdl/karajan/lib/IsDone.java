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

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.Path;

public class IsDone extends VDLFunction {
    public static final Arg STAGEOUT = new Arg.Positional("stageout");
    
    static {
        setArguments(IsDone.class, new Arg[] { STAGEOUT });
    }

    @Override
    protected Object function(VariableStack stack) throws ExecutionException {
        List files = TypeUtil.toList(STAGEOUT.getValue(stack));
        for (Object f : files) {
            List pv = TypeUtil.toList(f);
            Path p = (Path) pv.get(0);
            DSHandle handle = (DSHandle) pv.get(1);
            if (!IsLogged.isLogged(stack, handle, p)) {
                return Boolean.FALSE;
            }
        }
        if (files.isEmpty()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
