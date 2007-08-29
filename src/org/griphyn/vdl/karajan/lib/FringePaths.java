/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;

public class FringePaths extends VDLFunction {

	static {
		setArguments(FringePaths.class, new Arg[] { PA_VAR, OA_PATH });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			var = var.getField(parsePath(OA_PATH.getValue(stack), stack));
			Collection c = var.getFringePaths();
			return c;
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
		catch (HandleOpenException e) {
			throw new FutureNotYetAvailable(addFutureListener(stack, e.getSource()));
		}
	}
}
