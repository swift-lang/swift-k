/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class SetFieldValue extends VDLFunction {
	public static final Logger logger = Logger.getLogger(SetFieldValue.class);

	public static final Arg PA_VALUE = new Arg.Positional("value");

	static {
		setArguments(SetFieldValue.class, new Arg[] { OA_PATH, PA_VAR, PA_VALUE });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			DSHandle leaf = var.getField(path);
			Object value = PA_VALUE.getValue(stack);
			if (logger.isInfoEnabled()) {
				logger.info("Setting " + leaf + " to " + value);
			}
			synchronized (leaf) {
// TODO want to do a type check here, for runtime type checking
// and pull out the appropriate internal value from value if it
// is a DSHandle. There is no need (I think? maybe numerical casting?)
// for type conversion here; but would be useful to have
// type checking.
				// leaf.setValue(internalValue(leaf.getType(), value));
				leaf.setValue(value);
				closeShallow(stack, leaf);
			}
			return null;
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

}
