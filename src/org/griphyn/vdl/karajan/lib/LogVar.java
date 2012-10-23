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
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.restartLog.RestartLog;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;

public class LogVar extends VDLFunction {

	static {
		setArguments(LogVar.class, new Arg[] { PA_VAR, PA_PATH });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		Path path;
        Object p = PA_PATH.getValue(stack);
        if (p instanceof Path) {
            path = (Path) p;
        }
        else {
            path = Path.parse(TypeUtil.toString(p));
        }
        logVar(stack, var, path);
		return null;
	}
	
	public static void logVar(VariableStack stack, DSHandle var, Path path) throws VariableNotFoundException {
	    path = var.getPathFromRoot().append(path);
        String annotation;
        if(var.getMapper() != null) {
            annotation = "" + var.getMapper().map(path);
        } else {
            annotation = "unmapped";
        }
        RestartLog.LOG_CHANNEL.ret(stack, var.getRoot().getParam(MappingParam.SWIFT_RESTARTID)
                + "." + path.stringForm() + "!" + annotation);
	}
}
