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
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;
import org.griphyn.vdl.mapping.AbsFile;

public class InFileDirs extends AbstractSequentialWithArguments {
    public static final Arg STAGEINS = new Arg.Positional("stageins");
    
    static {
        setArguments(InFileDirs.class, new Arg[] { STAGEINS });
    }

    @Override
    protected void post(VariableStack stack) throws ExecutionException {
        List files = TypeUtil.toList(STAGEINS.getValue(stack));
        VariableArguments ret = ArgUtil.getVariableReturn(stack);
        for (Object f : files) { 
        	String path = (String) f;
        	AbsFile af = new AbsFile(path);
        	if ("file".equals(af.getProtocol())) {
                String dir = af.getDir();
                // there could be a clash here since
                // "/a/b/c.txt" would be remotely the same
                // as "a/b/c.txt". Perhaps absolute paths
                // should have a unique prefix.
                if (dir.startsWith("/") && dir.length() != 1) {
                	ret.append(dir.substring(1));
                }
                else if (dir.length() != 0) {
                    ret.append(dir);
                }
        	}
        	else {
        	    // also prepend host name to the path
        	    ret.append(af.getHost() + "/" + af.getDir());
        	}
        }
        super.post(stack);
    }
}
