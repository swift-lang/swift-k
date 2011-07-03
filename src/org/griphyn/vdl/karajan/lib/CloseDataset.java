/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class CloseDataset extends VDLFunction {
	public static final Logger logger = Logger.getLogger(CloseDataset.class);
	
	public static final Arg OA_CHILDREN_ONLY = new Arg.Optional("childrenOnly", Boolean.FALSE); 

	static {
		setArguments(CloseDataset.class, new Arg[] { PA_VAR, OA_PATH, OA_CHILDREN_ONLY });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		Path path = parsePath(OA_PATH.getValue(stack), stack);
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing " + var);
			}
			var = var.getField(path);
			
			if (TypeUtil.toBoolean(OA_CHILDREN_ONLY.getValue(stack))) {
			    closeChildren(stack, (AbstractDataNode) var);
			}
			else {
			    var.closeDeep();
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
		return null;
	}
}
