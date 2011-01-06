// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Sep 23, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.StateManager;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class CheckpointNode extends SequentialWithArguments {
	private static final Logger logger = Logger.getLogger(CheckpointNode.class);

	public static final Integer DEFAULT_CHECKPOINTING_INTERVAL = new Integer(120);

	public static final Arg A_FILE = new Arg.Positional("file", 0);
	public static final Arg A_AUTOMATIC = new Arg.Optional("automatic", Boolean.FALSE);
	public static final Arg A_TIMESTAMPED = new Arg.Optional("timestamped", Boolean.FALSE);
	public static final Arg A_NOW = new Arg.Optional("now", Boolean.FALSE);
	public static final Arg A_INTERVAL = new Arg.Optional("interval",
			DEFAULT_CHECKPOINTING_INTERVAL);

	static {
		setArguments(CheckpointNode.class, new Arg[] { A_FILE, A_AUTOMATIC, A_TIMESTAMPED, A_NOW,
				A_INTERVAL });
	}

	public void post(VariableStack stack) throws ExecutionException {
		try {
			StateManager cm = stack.getExecutionContext().getStateManager();
			cm.registerElement(this, stack);

			String fileName = TypeUtil.toString(A_FILE.getValue(stack));

			cm.setName(fileName);
			cm.setInterval(TypeUtil.toInteger(A_INTERVAL.getValue(stack)));
			cm.setCheckpointsEnabled(TypeUtil.toBoolean(A_AUTOMATIC.getValue(stack)));
			cm.setTimestamped(TypeUtil.toBoolean(A_TIMESTAMPED.getValue(stack)));

			boolean now = TypeUtil.toBoolean(A_NOW.getValue(stack));
			if (now) {
				cm.checkpoint(fileName);
			}
			stack.setVar("#checkpointManager", cm);
			stack.exportVar("#checkpointManager");
			cm.unregisterElement(this, stack);
			complete(stack);
		}
		catch (ExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ExecutionException(stack, "Failed to create checkpoint! (" + e.getMessage()
					+ ")", e);
		}
	}
}