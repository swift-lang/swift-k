/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Collection;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class GetField extends VDLFunction {
	static {
		setArguments(GetField.class, new Arg[] { OA_PATH, PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			Collection fields = var.getFields(path);
			if(fields.size() == 1) {
				return fields.toArray()[0];
			} else {
				return fields;
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
		catch (HandleOpenException e) {
			throw new ExecutionException(e);
		}
	}


}
