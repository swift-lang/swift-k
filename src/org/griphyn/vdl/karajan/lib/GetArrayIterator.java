package org.griphyn.vdl.karajan.lib;

import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.PairIterator;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class GetArrayIterator extends VDLFunction {
	public static final Logger logger = Logger.getLogger(GetArrayIterator.class);

	static {
		setArguments(GetArrayIterator.class, new Arg[] { PA_VAR, OA_PATH });
	}

	/**
	 * Takes a supplied variable and path, and returns an array iterator.
	 */
	public Object function(VariableStack stack) throws ExecutionException {
		Object var1 = PA_VAR.getValue(stack);
		if (!(var1 instanceof DSHandle)) {
			return var1;
		}
		DSHandle var = (DSHandle) var1;
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			if (path.hasWildcards()) {
				throw new RuntimeException("Wildcards not supported");
			}
			else {
				var = var.getField(path);
				if (var.getType().isArray()) {
					Map value = var.getArrayValue();
					if (var.isClosed()) {
						return new PairIterator(value);
					}
					else {
						synchronized(var.getRoot()) {
							return addFutureListListener(stack, var, value);
						}
					}
				} else {
					throw new RuntimeException("Cannot get array iterator for non-array");
				}
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

}
