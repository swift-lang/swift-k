/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;

public class FileSet extends VDLFunction {
	static {
		setArguments(FileSet.class, new Arg[] { PA_VAR, OA_PATH });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			return var.getField(parsePath(OA_PATH.getValue(stack), stack)).getFileSet();
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}
}
