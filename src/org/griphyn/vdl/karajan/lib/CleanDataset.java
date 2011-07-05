/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;

public class CleanDataset extends VDLFunction {
	public static final Logger logger = Logger.getLogger(CleanDataset.class);
	
	static {
		setArguments(CleanDataset.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		AbstractDataNode var = (AbstractDataNode) PA_VAR.getValue(stack);
		logger.info("Cleaning " + var);
		var.clean();
		return null;
	}
}
