/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.Path;

public class SetFutureFault extends VDLFunction {
	public static final Logger logger = Logger.getLogger(SetFutureFault.class);

	public static final Arg OA_EXCEPTION = new Arg.Optional("exception", null);
	public static final Arg OA_MAPPING = new Arg.Optional("mapping", Boolean.FALSE);

	static {
		setArguments(SetFutureFault.class, new Arg[] { OA_PATH, PA_VAR, OA_EXCEPTION, OA_MAPPING });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		boolean mapping = TypeUtil.toBoolean(OA_MAPPING.getValue(stack));
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			DSHandle leaf = var.getField(path);
			if (logger.isInfoEnabled()) {
				logger.info("Failing " + leaf + " (mapping=" + mapping + ")");
			}
			synchronized (leaf) {
				Object value = OA_EXCEPTION.getValue(stack);
				if (mapping) {
					leaf.setValue(new MappingDependentException(leaf, (Exception) value));
				}
				else {
					leaf.setValue(new DataDependentException(leaf, (Exception) value));
				}
			}
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}
		return null;
	}
}
