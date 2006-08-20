// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadedClassRunner;
import org.globus.cog.karajan.util.ThreadedClassRunnerListener;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class ExecuteJavaNode extends SequentialWithArguments implements ThreadedClassRunnerListener {
	public static final Logger logger = Logger.getLogger(ExecuteJavaNode.class);

	public static final Arg A_CLASS = new Arg.Positional("class", 0);

	private final Map map;

	static {
		setArguments(ExecuteJavaNode.class, new Arg[] { A_CLASS, Arg.VARGS });
	}

	public ExecuteJavaNode() {
		map = new HashMap();
	}

	public void post(VariableStack stack) throws ExecutionException {
		Object[] args = Arg.VARGS.asArray(stack);
		String[] arguments = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			arguments[i] = TypeUtil.toString(args[i]);
		}
		ThreadedClassRunner tcl = new ThreadedClassRunner(this,
				TypeUtil.toString(A_CLASS.getValue(stack)), arguments);
		map.put(tcl, stack.copy());
		tcl.start();
	}

	public void failed(ThreadedClassRunner source, Throwable reason) {
		VariableStack stack = (VariableStack) map.get(source);
		map.remove(source);
		try {
			failImmediately(stack, reason.getMessage());
		}
		catch (ExecutionException e) {
			logger.fatal("ExecutionException caught out of execution context", e);
		}
	}

	public void completed(ThreadedClassRunner source) {
		VariableStack stack = (VariableStack) map.get(source);
		map.remove(source);
		try {
			super.complete(stack);
		}
		catch (ExecutionException e) {
			logger.fatal("ExecutionException caught out of execution context", e);
		}
	}
}
