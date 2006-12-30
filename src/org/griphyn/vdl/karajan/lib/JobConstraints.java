/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.TaskConstraints;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.util.FQN;

public class JobConstraints extends VDLFunction {
	public static final Arg A_TR = new Arg.Positional("tr");

	static {
		setArguments(JobConstraints.class, new Arg[] { A_TR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		String tr = TypeUtil.toString(A_TR.getValue(stack));
		TaskConstraints tc = new TaskConstraints();
		tc.addConstraint("tr", tr);
		tc.addConstraint("trfqn", new FQN(tr));
		return tc;
	}
}
