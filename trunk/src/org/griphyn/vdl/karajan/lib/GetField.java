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

/** 
 * Obtain the DSHandle from within another DSHandle via the given PATH
 * */
public class GetField extends VDLFunction {
	static {
		setArguments(GetField.class, new Arg[] { OA_PATH, PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		Object var1 = PA_VAR.getValue(stack);

		if(var1 instanceof DSHandle) {

			try {
				DSHandle var = (DSHandle) var1;

				Path path = parsePath(OA_PATH.getValue(stack), stack);
				DSHandle field = var.getField(path);
				return field;
			}
			catch (InvalidPathException e) {
				throw new ExecutionException(e);
			}
		} else {
			throw new ExecutionException("was expecting a DSHandle, got: "+var1.getClass());
		}
	}
}
