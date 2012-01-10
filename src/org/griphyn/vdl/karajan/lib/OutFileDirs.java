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
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class OutFileDirs extends VDLFunction {
    public static final Arg STAGEOUTS = new Arg.Positional("stageouts");

    static {
        setArguments(OutFileDirs.class, new Arg[] { STAGEOUTS });
    }

    @Override
    public Object function(VariableStack stack) throws ExecutionException {
        List files = TypeUtil.toList(STAGEOUTS.getValue(stack));
        VariableArguments ret = ArgUtil.getVariableReturn(stack);
        try {
            for (Object f : files) {
                List pv = TypeUtil.toList(f);
                Path p = parsePath(pv.get(0), stack);
                DSHandle handle = (DSHandle) pv.get(1);
                DSHandle leaf = handle.getField(p);
                String fname = VDLFunction.filename(leaf)[0];
                String dir = new AbsFile(fname).getDir();
                if (dir.startsWith("/") && dir.length() != 1) {
                    ret.append(dir.substring(1));
                }
                else if (dir.length() != 0) {
                    ret.append(dir);
                }
            }
        }
        catch (Exception e) {
            throw new ExecutionException(e);
        }
        return null;
    }
}
