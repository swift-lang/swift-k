/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.restartLog.RestartLog;

public class LogVar extends VDLFunction {
	static {
		setArguments(LogVar.class, new Arg[] { PA_VAR, PA_PATH });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		RestartLog.LOG_CHANNEL.ret(stack, getFileName(stack));
		return null;
	}
}
