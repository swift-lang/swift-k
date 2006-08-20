// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 23, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;

public class RestartOnErrorNode extends PartialArgumentsContainer {
	public static final Logger logger = Logger.getLogger(RestartOnErrorNode.class);
	
	public static final Arg A_MATCH = new Arg.Positional("match", 0);
	public static final Arg A_TIMES = new Arg.Positional("times", 1);

	private static final String MATCH = "##match";

	static {
		setArguments(RestartOnErrorNode.class, new Arg[] { A_MATCH, A_TIMES });
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		if (A_MATCH.isPresent(stack)) {
			stack.setVar(MATCH, A_MATCH.getValue(stack));
		}
		else {
			stack.setVar(MATCH, ".*");
		}
		stack.setVar("#restartTimes", new Integer(TypeUtil.toInt(A_TIMES.getValue(stack))));
		super.partialArgumentsEvaluated(stack);
		startRest(stack);
	}

	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		if (NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
			VariableStack stack = e.getStack();

			if (!matches(stack, (FailureNotificationEvent) e)) {
				super.notificationEvent(e);
				return;
			}

			int itimes = stack.currentFrame().preDecrementAtomic("#restartTimes");
			if (itimes >= 0) {
				logger.debug("Restarting. " + itimes + " times left.");
				logger.debug("Stack size: " + stack.frameCount());
				this.startRest(stack);
				return;
			}
			else {
				logger.debug("Failed too many times.");
			}
		}
		super.notificationEvent(e);
	}
	
	protected boolean matches(VariableStack stack, FailureNotificationEvent e) {
		if (!stack.currentFrame().isDefined(MATCH)) {
			return false;
		}
		else {
			Object match = stack.currentFrame().getVar(MATCH);
			if (match instanceof List) {
				Iterator i = ((List) match).iterator();
				while (i.hasNext()) {
					if (matches(TypeUtil.toString(i.next()), e)){
						return true;
					}
				}
				return false;
			}
			else {
				return matches(TypeUtil.toString(match), e);
			}
		}
	}
	
	protected boolean matches(String str, FailureNotificationEvent e) {
		return e.getMessage().matches(str);
	}
}
