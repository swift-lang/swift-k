// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.Arguments;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.NamedArgumentsImpl;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.Trace;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;
import org.globus.cog.karajan.workflow.nodes.user.UDEDefinition;
import org.globus.cog.karajan.workflow.nodes.user.UserDefinedElement;

public class ExecuteElement extends SequentialWithArguments {
	public static final Logger logger = Logger.getLogger(ExecuteElement.class);
	
	public static final Arg A_ELEMENT = new Arg.Positional("element", 0);
	public static final Arg A_ARGS = new Arg.Optional("args");
	
	private static int count = 0;

	private UserDefinedElement cached;

	private boolean initialized, vargs, named;

	static {
		setArguments(ExecuteElement.class, new Arg[] { A_ELEMENT, A_ARGS, Arg.VARGS });
	}

	public ExecuteElement() {
		setOptimize(false);
	}

	public void post(VariableStack stack) throws ExecutionException {
		Object element = A_ELEMENT.getValue(stack);
		if (element instanceof UDEDefinition) {
			startDef(stack, (UDEDefinition) element);
		}
		else if (element instanceof FlowElement) {
			stack.leave();
			if (logger.isDebugEnabled()) {
				threadTracker.remove(ThreadingContext.get(stack));
			}
			ThreadingContext.set(stack, ThreadingContext.get(stack).split(getIntProperty(UID)));
			if (element instanceof ExtendedFlowElement && ((ExtendedFlowElement) element).isSimple()) {
				((ExtendedFlowElement) element).executeSimple(stack);
				fireNotificationEvent(new NotificationEvent(this,
						NotificationEventType.EXECUTION_COMPLETED, stack), stack);
			}
			else {
				startElement((FlowElement) element, stack);
			}
		}
		else {
			throw new ExecutionException("Cannot execute element of type " + element.getClass());
		}
	}

	protected void startDef(VariableStack stack, UDEDefinition def) throws ExecutionException {
		Arguments args = getArguments(stack, def.getUde());
		stack.leave();
		stack.enter();
		stack.setVar(DefUtil.ENV, def.getEnv());
		stack.setVar(Trace.ELEMENT, this);
		if (logger.isDebugEnabled()) {
			threadTracker.remove(ThreadingContext.get(stack));
		}
		def.getUde().startBody(stack, args);
	}

	protected Arguments getArguments(VariableStack stack, UserDefinedElement def)
			throws ExecutionException {
		Arguments args = new Arguments();
		VariableArguments vargs = ArgUtil.getVariableArguments(stack);
		NamedArguments named = ArgUtil.getNamedArguments(stack);
		if (named.hasArgument(A_ELEMENT.getName())) {
			named.add(A_ELEMENT.getName(), null);
		}
		else {
			vargs.removeFirst();
		}
		if (A_ARGS.isPresent(stack)) {
			named = new NamedArgumentsImpl();
			named.addAll((Map) checkClass(A_ARGS.getValue(stack), Map.class, "map"));
		}
		args.setNamed(named);
		args.setVargs(vargs);
		return args;
	}
}