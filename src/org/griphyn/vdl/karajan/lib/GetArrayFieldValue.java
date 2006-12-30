/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Map;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.PairIterator;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class GetArrayFieldValue extends VDLFunction {
	static {
		setArguments(GetArrayFieldValue.class, new Arg[] { PA_VAR, OA_PATH });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			synchronized (var) {
				var = var.getField(path);
				Map value = var.getArrayValue();
				if (var.isClosed()) {
					// System.err.println("gafv: " + var + "." + path + " ->
					// OKIT[]");
					return new PairIterator(value);
				}
				else {
					return addFutureListListener(stack, var, value);
				}
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

}
