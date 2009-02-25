package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class WaitFieldValue extends VDLFunction {
	public static final Logger logger = Logger.getLogger(WaitFieldValue.class);

	static {
		setArguments(WaitFieldValue.class, new Arg[] { PA_VAR, OA_PATH });
	}

	/**
	 * Takes a supplied variable and path, and returns the unique value at that
	 * path. Path can contain wildcards, in which case an array is returned.
	 */
	public Object function(VariableStack stack) throws ExecutionException {
		Object var1 = PA_VAR.getValue(stack);
		if (!(var1 instanceof DSHandle)) {
			throw new RuntimeException("Can only wait for DSHandles - was supplied "+var1.getClass());
		}
		DSHandle var = (DSHandle) var1;
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			var = var.getField(path);
			synchronized (var.getRoot()) {
				if (!var.isClosed()) {
					logger.debug("Waiting for " + var);
					throw new FutureNotYetAvailable(addFutureListener(stack, var));
				}
				else {
					Object v = var.getValue();
					logger.debug("Do not need to wait for " + var+" as it is closed and has value "+v + (v!=null ? " with class "+v.getClass() : "" ));
					if(v !=null && v instanceof RuntimeException) {
						throw (RuntimeException)v;
					} else {
						return null;
					}
				}
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

}
