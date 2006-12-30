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
