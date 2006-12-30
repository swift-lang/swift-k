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

public class CloseDataset extends VDLFunction {
	static {
		setArguments(CloseDataset.class, new Arg[] { PA_VAR, OA_PATH });
	}

	// TODO path is not used!
	public Object function(VariableStack stack) throws ExecutionException {
		Path path = parsePath(OA_PATH.getValue(stack), stack);
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			var = var.getField(path);
			closeChildren(stack, var);
			return null;
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}
}
