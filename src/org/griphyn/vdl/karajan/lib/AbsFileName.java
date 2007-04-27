/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class AbsFileName extends VDLFunction {
	static {
		setArguments(AbsFileName.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		return filename(stack);
	}
}
