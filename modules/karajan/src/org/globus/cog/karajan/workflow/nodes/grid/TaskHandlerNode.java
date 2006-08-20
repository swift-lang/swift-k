// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TaskHandlerWrapper;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;

public class TaskHandlerNode extends AbstractFunction {
	public static final Arg A_TYPE = new Arg.Positional("type");
	public static final Arg A_PROVIDER = new Arg.Positional("provider");
	public static final Arg.Channel HANDLERS_CHANNEL = new Arg.Channel("handlers");

	static {
		setArguments(TaskHandlerNode.class,
				new Arg[] { A_TYPE, A_PROVIDER });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		TaskHandlerWrapper th = new TaskHandlerWrapper();
		String type = TypeUtil.toString(A_TYPE.getValue(stack));
		String provider = TypeUtil.toString(A_PROVIDER.getValue(stack));

		if (type.equals("execution") || type.equals("job-submission")) {
			th.setType(TaskHandler.EXECUTION);
		}
		else if (type.equals("file") || type.equals("file-operation")) {
			th.setType(TaskHandler.FILE_OPERATION);
		}
		else if (type.equals("file-transfer")) {
			th.setType(TaskHandler.FILE_TRANSFER);
		}
		else {
			throw new ExecutionException("Unsupported type: " + type
					+ ". Must be either 'execution' or 'file'");
		}

		th.setProvider(provider);

		HANDLERS_CHANNEL.ret(stack, th);
		return null;
	}
}