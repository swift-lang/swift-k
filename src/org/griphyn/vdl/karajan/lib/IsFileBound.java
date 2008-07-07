/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;

public class IsFileBound extends VDLFunction {
	static {
		setArguments(IsFileBound.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		if (var instanceof AbstractDataNode) {
			return Boolean.valueOf(!((AbstractDataNode) var).isPrimitive());
		}
		else {
			return Boolean.FALSE;
		}
	}

}
