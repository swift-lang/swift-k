/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class SetFieldValue extends VDLFunction {
	public static final Arg PA_VALUE = new Arg.Positional("value");

	static {
		setArguments(SetFieldValue.class, new Arg[] { OA_PATH, PA_VAR, PA_VALUE });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			DSHandle leaf = var.getField(path);
			synchronized (leaf) {
				leaf.setValue(PA_VALUE.getValue(stack));
				closeShallow(stack, leaf);
			}
			return null;
		}
		catch (Exception e) {
			throw new ExecutionException(e.getMessage() + " for variable " + var, e);
		}
	}

}
