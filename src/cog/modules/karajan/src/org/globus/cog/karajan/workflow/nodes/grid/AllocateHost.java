// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.PartialArgumentsContainer;

public class AllocateHost extends PartialArgumentsContainer {
	public static final Logger logger = Logger.getLogger(AllocateHost.class);

	public static final Arg A_NAME = new Arg.Positional("name");
	public static final Arg A_CONSTRAINTS = new Arg.Optional("constraints", null);
	
	static {
		setArguments(AllocateHost.class, new Arg[] { A_NAME, A_CONSTRAINTS });
	}

	public AllocateHost() {
		this.setQuotedArgs(true);
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		Object constraints = A_CONSTRAINTS.getValue(stack); 
		try {
			Scheduler s = (Scheduler) stack.getDeepVar(SchedulerNode.SCHEDULER);
			Contact contact = s.allocateContact(constraints);
			logger.debug("Allocated host " + contact);
			stack.setVar(TypeUtil.toString(A_NAME.getValue(stack)), contact);
		}
		catch (ExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			fail(stack, e.getMessage());
			return;
		}
		super.partialArgumentsEvaluated(stack);
		startRest(stack);
	}
}
