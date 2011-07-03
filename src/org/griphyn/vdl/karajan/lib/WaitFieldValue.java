package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;
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
		AbstractDataNode var = (AbstractDataNode) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			var = (AbstractDataNode) var.getField(path);
			var.waitFor();
			return null;
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

}
