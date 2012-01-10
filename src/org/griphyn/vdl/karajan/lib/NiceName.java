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
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class NiceName extends VDLFunction {
	static {
		setArguments(NiceName.class, new Arg[] { OA_PATH, PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			DSHandle field = var.getField(path);
			Path p = field.getPathFromRoot();
			if (p.equals(Path.EMPTY_PATH)) {
				Object dbgname = field.getRoot().getParam("dbgname");
				if (dbgname == null) {
					return "tmp"+field.getRoot().hashCode();
				}
				else {
					return dbgname;
				}
			}
			else {
				return field.getRoot().getParam("dbgname") + "." + p;
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}


}
