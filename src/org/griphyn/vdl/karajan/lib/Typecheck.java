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
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class Typecheck extends VDLFunction {
	public static final Arg PA_TYPE = new Arg.Positional("type");
	public static final Arg OA_ARGNAME = new Arg.Optional("argname");

	static {
		setArguments(Typecheck.class, new Arg[] { PA_VAR, PA_TYPE, OA_ARGNAME });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		String type = TypeUtil.toString(PA_TYPE.getValue(stack));
		Object ovar = PA_VAR.getValue(stack);
		if (!(ovar instanceof DSHandle)) {
			throw new ExecutionException("Wrong java type for argument. "
					+ "Expected DSHandle containing " + type
					+ "; got java object of class " + ovar.getClass() + " with value " + ovar);
		}
		DSHandle var = (DSHandle) ovar;
		String argname = TypeUtil.toString(OA_ARGNAME.getValue(stack, null));

		try {
			Type t = Types.getType(type);
			if (!compatible(t, var.getType())) {
				if (argname != null) {
					throw new ExecutionException("Wrong type for argument '" + argname + "'. Expected "
							+ type + "; got " + var.getType() + ". Actual argument: " + var);
				}
				else {
					throw new ExecutionException("Wrong type for argument. Expected " + type + "; got "
							+ var.getType() + ". Actual argument: " + var);
				}
			}
		}
		catch (NoSuchTypeException e) {
			throw new ExecutionException(e);
		}
		return null;
	}
}
