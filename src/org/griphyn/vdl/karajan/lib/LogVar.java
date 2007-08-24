/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.restartLog.RestartLog;

public class LogVar extends VDLFunction {
	public static final Arg PA_HOST = new Arg.Positional("host");
	public static final Arg PA_DIR = new Arg.Positional("dir");
	public static final Arg PA_NAME = new Arg.Positional("name");

	static {
		setArguments(LogVar.class, new Arg[] { PA_VAR, PA_PATH, PA_HOST, PA_DIR, PA_NAME });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		RestartLog.LOG_CHANNEL.ret(stack, getFileName(stack) + "/" + PA_HOST.getValue(stack) + "/"
				+ PA_DIR.getValue(stack) + "/" + PA_NAME.getValue(stack));
		return null;
	}
}
