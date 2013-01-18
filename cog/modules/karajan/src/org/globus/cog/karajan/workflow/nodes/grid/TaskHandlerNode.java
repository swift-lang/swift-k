// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TaskHandlerWrapper;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;

public class TaskHandlerNode extends AbstractFunction {

	private static final Map ptypes, wtypes;

	static {
		ptypes = new HashMap();
		ptypes.put("execution", AbstractionProperties.TYPE_EXECUTION_TASK_HANDLER);
		ptypes.put("job-submission", AbstractionProperties.TYPE_EXECUTION_TASK_HANDLER);
		ptypes.put("file-transfer", AbstractionProperties.TYPE_FILE_TRANSFER_TASK_HANDLER);
		ptypes.put("file", AbstractionProperties.TYPE_FILE_OPERATION_TASK_HANDLER);
		ptypes.put("file-operation", AbstractionProperties.TYPE_FILE_OPERATION_TASK_HANDLER);

		wtypes = new HashMap();
		wtypes.put(AbstractionProperties.TYPE_EXECUTION_TASK_HANDLER, new Integer(
				TaskHandler.EXECUTION));
		wtypes.put(AbstractionProperties.TYPE_FILE_TRANSFER_TASK_HANDLER, new Integer(
				TaskHandler.FILE_TRANSFER));
		wtypes.put(AbstractionProperties.TYPE_FILE_OPERATION_TASK_HANDLER, new Integer(
				TaskHandler.FILE_OPERATION));
	}

	public static String karajanToAbstractionType(String type) {
		String atype = (String) ptypes.get(type);
		if (atype == null) {
			throw new IllegalArgumentException("Unsupported type: " + type
					+ ". Supported types are: \"execution\", \"file\", and \"file-transfer\"");
		}
		else {
			return atype;
		}
	}

	public static int abstractionToHandlerType(String type) {
		try {
			return ((Integer) wtypes.get(type)).intValue();
		}
		catch (NullPointerException e) {
			throw new IllegalArgumentException("Invalid abstraction handler type: " + type);
		}
	}
		
	public static int karajanToHandlerType(String type) {
		return abstractionToHandlerType(karajanToAbstractionType(type));
	}
	
	public static final Arg A_TYPE = new Arg.Positional("type");
	public static final Arg A_PROVIDER = new Arg.Positional("provider");
	public static final Arg.Channel HANDLERS_CHANNEL = new Arg.Channel("handlers");

	static {
		setArguments(TaskHandlerNode.class, new Arg[] { A_TYPE, A_PROVIDER });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		TaskHandlerWrapper th = new TaskHandlerWrapper();
		String type = TypeUtil.toString(A_TYPE.getValue(stack)).toLowerCase();
		String provider = TypeUtil.toString(A_PROVIDER.getValue(stack));
		
		th.setType(karajanToHandlerType(type));
		th.setProvider(provider);

		HANDLERS_CHANNEL.ret(stack, th);
		return null;
	}
}