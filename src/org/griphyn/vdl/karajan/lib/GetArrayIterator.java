package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.PairIterator;
import org.griphyn.vdl.mapping.ArrayDataNode;
import org.griphyn.vdl.mapping.DSHandle;
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
				if (!var.getType().isArray()) {
				    throw new RuntimeException("Cannot get array iterator for non-array");
				}
				synchronized(var) {
					if (var.isClosed()) {
						return new PairIterator(var.getArrayValue());
					}
					else {
						return ((ArrayDataNode) var).getFutureList().futureIterator();
					}
				}
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

}
