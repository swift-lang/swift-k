/*
 * Created on Dec 26, 2006
 */
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

public class GetFieldValue extends VDLFunction {
	public static final Logger logger = Logger.getLogger(GetFieldValue.class);

	static {
		setArguments(GetFieldValue.class, new Arg[] { PA_VAR, OA_PATH });
	}

	/**
	 * Takes a supplied variable and path, and returns the unique value at that
	 * path. Path can contain wildcards, in which case an array is returned.
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
				try {
					return var.getFields(path).toArray();
				}
				catch (HandleOpenException e) {
					if (logger.isDebugEnabled())
						logger.debug("Waiting for var=" + var + " path=" + path);
					throw new FutureNotYetAvailable(addFutureListener(stack, e.getSource()));
				}
			}
			else {
				var = var.getField(path);
				if (var.getType().isArray()) {
					// this bit from GetArrayFieldValue
					Map value = var.getArrayValue();
					if (var.isClosed()) {
						return new PairIterator(value);
					}
					else {
						return addFutureListListener(stack, var, value);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("GetFieldValue(" + var + ")");
				}
				synchronized (var) {
					if (!var.isClosed()) {
						if (logger.isDebugEnabled()) {
							logger.debug("Waiting for " + var);
						}
						throw new FutureNotYetAvailable(addFutureListener(stack, var));
					}
					else {
						return var.getValue();
					}
				}
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

}
