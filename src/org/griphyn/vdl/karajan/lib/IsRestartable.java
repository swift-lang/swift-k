package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;

/** Determines if a variable is 'restartable'; that is, if we restart the
    workflow, will this variable still have its content.
*/
    
    

public class IsRestartable extends VDLFunction {
	static {
		setArguments(IsRestartable.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		return Boolean.valueOf(var.isRestartable());
	}
}

