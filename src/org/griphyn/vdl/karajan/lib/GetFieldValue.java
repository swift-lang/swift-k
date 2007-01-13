/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class GetFieldValue extends VDLFunction {
	public static final Arg PA_VAR1 = new Arg.Positional("var");

	static {
		setArguments(GetFieldValue.class, new Arg[] { PA_VAR1, OA_PATH });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		Object var1 = PA_VAR1.getValue(stack);
		if (!(var1 instanceof DSHandle)) {
			return var1;
		}
		DSHandle var = (DSHandle) var1;
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			if (path.hasWildcards()) {
				try {
					return var.getFields(path).toArray();
				}
				catch (HandleOpenException e) {
					throw new FutureNotYetAvailable(addFutureListener(stack, e.getSource()));
				}
			}
			else {
				var = var.getField(path);
				if (var.isArray()) {
					throw new ExecutionException("getfieldvalue called on an array: " + var);
				}
				synchronized (var) {
					Object value = var.getValue();
					if (!var.isClosed()) {
						throw new FutureNotYetAvailable(addFutureListener(stack, var));
					}
					else {
						return value;
					}
				}
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

}
