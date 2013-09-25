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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureFault;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class GetFieldValue extends VDLFunction {
	public static final Logger logger = Logger.getLogger(GetFieldValue.class);

	static {
		setArguments(GetFieldValue.class, new Arg[] { PA_VAR, OA_PATH });
	}

	/**
	 * Takes a supplied variable and path, and returns the unique value at that
	 * path. Path can contain wildcards, in which case an array is returned.
	 */
	public Object function(VariableStack stack) throws ExecutionException {
		Object var1 = PA_VAR.getValue(stack);
		if (!(var1 instanceof DSHandle)) {
			return var1;
		}
		AbstractDataNode var = (AbstractDataNode) var1;

		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			if (path.hasWildcards()) {
			    return var.getFields(path).toArray();
			}
			else {
				var = (AbstractDataNode) var.getField(path);
				if (var.getType().isArray()) {
					throw new RuntimeException("Getting value for array " + var + " which is not permitted.");
				}
				var.waitFor();
				return var.getValue();
			}
		}
		catch (FutureFault f) {
		    throw f;
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}
	}
}
