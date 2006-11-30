// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.ContactAllocationTask;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.PartialArgumentsContainer;

public class AllocateHost extends PartialArgumentsContainer implements StatusListener {
	public static final Logger logger = Logger.getLogger(AllocateHost.class);

	public static final Arg A_NAME = new Arg.Positional("name");
	public static final Arg A_CONSTRAINTS = new Arg.Optional("constraints", null);
	
	public static final String HOST = "##host";

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
			if (constraints == null) {
				Contact contact = s.allocateContact();
				if (logger.isDebugEnabled()) {
					logger.debug("Allocated host " + contact);
				}
				stack.setVar(TypeUtil.toString(A_NAME.getValue(stack)), contact);
				super.partialArgumentsEvaluated(stack);
				startRest(stack);
			}
			else {
				ContactAllocationTask t = new ContactAllocationTask();
				t.setStack(stack);
				Contact contact = s.allocateContact(constraints);
				s.addJobStatusListener(this, t);
				s.enqueue(t, new Contact[] { contact });
			}
		}
		catch (ExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			fail(stack, e.getMessage());
		}
	}

	public void statusChanged(StatusEvent event) {
		ContactAllocationTask t = (ContactAllocationTask) event.getSource();
		VariableStack stack = t.getStack();
		try {
			int code = event.getStatus().getStatusCode();
			if (code == Status.FAILED) {
				Exception e = event.getStatus().getException();
				if (e == null) {
					failImmediately(stack, "Failed to allocate host");
				}
				else {
					failImmediately(stack, event.getStatus().getException());
				}
			}
			else if (code == Status.COMPLETED) {
				stack.setVar(TypeUtil.toString(A_NAME.getValue(stack)), t.getContact());
				stack.setVar(HOST, t.getContact());
				super.partialArgumentsEvaluated(stack);
				startRest(stack);
			}
		}
		catch (ExecutionException e) {
			try {
				failImmediately(stack, e);
			}
			catch (ExecutionException e1) {
				logger.warn("Could not fail element", e1);
			}
		}
	}

	protected void _finally(VariableStack stack) throws ExecutionException {
		super._finally(stack);
		Scheduler s = (Scheduler) stack.getDeepVar(SchedulerNode.SCHEDULER);
		Contact c = (Contact) stack.currentFrame().getVar(HOST);
		if (s != null && c instanceof BoundContact) {
			s.releaseContact((BoundContact) c);
		}
	}
}
