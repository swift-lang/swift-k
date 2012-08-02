// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 3, 2004
 */
package org.globus.cog.karajan.workflow.nodes.grid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.scheduler.submitQueue.NonBlockingSubmit;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.nodes.SequentialWithArguments;

public abstract class AbstractGridNode extends SequentialWithArguments implements StatusListener {
	private static final Logger logger = Logger.getLogger(AbstractGridNode.class);

	public static final String HANDLER = "#task:handler";

	private Map<Task, VariableStack> tasks;
	private Map dynamicHosts;

	public AbstractGridNode() {
		tasks = new HashMap<Task, VariableStack>();
		dynamicHosts = new HashMap();
	}

	public final void post(VariableStack stack) throws ExecutionException {
		stack.getExecutionContext().getStateManager().registerElement(this, stack);
		try {
			submitTask(stack);
		}
		catch (ExecutionException e) {
			stack.getExecutionContext().getStateManager().unregisterElement(this, stack);
			throw e;
		}
	}

	protected abstract void submitTask(VariableStack stack) throws ExecutionException;

	public Scheduler getScheduler(VariableStack stack) throws ExecutionException {
		return (Scheduler) stack.getGlobal(SchedulerNode.SCHEDULER);
	}

	protected Contact getHost(VariableStack stack, Arg hostarg, Scheduler scheduler, String provider)
			throws ExecutionException {
		Object value = hostarg.getValue(stack);
		if (value instanceof String) {
			String name = (String) value;
			if ("localhost".equalsIgnoreCase(name)) {
				return BoundContact.LOCALHOST;
			}
			else {
				if (scheduler != null) {
					Contact contact = scheduler.getResources().getContact(name);
					if (contact != null) {
						return contact;
					}
				}

				BoundContact bc;

				synchronized (dynamicHosts) {
					bc = (BoundContact) dynamicHosts.get(name);
					if (bc == null) {
						bc = new BoundContact(name);
						dynamicHosts.put(name, bc);
					}
				}

				if (!bc.hasService(Service.EXECUTION, provider)) {
					if (provider != null) {
						addDefaultServices(bc, name, provider);
					}
				}
				return bc;
			}
		}
		else if (value instanceof Contact) {
			return (Contact) value;
		}
		else {
			throw new ExecutionException("The " + hostarg.getName()
					+ " argument must either be a name of a host known by the "
					+ "current scheduler, or a host object");
		}
	}

	private void addDefaultServices(BoundContact bc, String contact, String provider)
			throws ExecutionException {
	    
	    ServiceContact serviceContact = new ServiceContactImpl(contact);
		SecurityContext sc = null;
		if (provider != null) {
			try {
				sc = AbstractionFactory.getSecurityContext(provider, serviceContact);
			}
			catch (Exception e) {
				throw new ExecutionException("Could not get default security context for provider "
						+ provider, e);
			}
		}
		bc.addService(new ServiceImpl(provider, Service.EXECUTION, serviceContact, sc));
		bc.addService(new ServiceImpl(provider, Service.FILE_OPERATION, serviceContact, sc));
		bc.addService(new ServiceImpl(provider, Service.FILE_TRANSFER, serviceContact, sc));
	}

	public static final Arg A_SECURITY_CONTEXT = new Arg.TypedPositional("securityContext",
			SecurityContext.class, "Security Context");

	public void setSecurityContext(VariableStack stack, Service service) throws ExecutionException {
		if (A_SECURITY_CONTEXT.isPresent(stack)) {
			SecurityContext sc = (SecurityContext) A_SECURITY_CONTEXT.getValue(stack);
			service.setSecurityContext(sc);
		}
		else {
			if (stack.isDefined("#securitycontext")) {
				SecurityContext sc = (SecurityContext) this.checkClass(
						stack.getVar("#securitycontext"), SecurityContext.class, "Security Context");
				service.setSecurityContext(sc);
			}
		}
	}

	public void abort(VariableStack stack) throws ExecutionException {
		Set<Task> t;
		synchronized (tasks) {
			t = new HashSet<Task>(tasks.keySet());
		}
		for (Iterator i = t.iterator(); i.hasNext();) {
			Task task = (Task) i.next();
			VariableStack vs = tasks.get(task);
			if (ThreadingContext.get(vs).isSubContext(ThreadingContext.get(stack))) {
				Scheduler scheduler;
				try {
					TaskHandler th = (TaskHandler) vs.currentFrame().getVar(HANDLER);
					try {
						th.cancel(task);
					}
					catch (Exception exx) {
						throw new ExecutionException("Could not cancel task", exx);
					}
				}
				catch (VariableNotFoundException ex) {
					scheduler = (Scheduler) vs.getGlobal(SchedulerNode.SCHEDULER);
					scheduler.cancelTask(task);
				}
			}
		}
	}

	protected final void setStack(Task task, VariableStack stack) {
		synchronized (tasks) {
			tasks.put(task, stack);
		}
	}

	public void submitUnscheduled(TaskHandler handler, Task task, VariableStack stack)
			throws ExecutionException {
		setTaskIdentity(stack, task);
		task.addStatusListener(this);
		stack.setVar(HANDLER, handler);
		setStack(task, stack);
		try {
			new NonBlockingSubmit(handler, task, null).go();
		}
		catch (Exception e) {
			// avoid failing twice if status is already set
			if (task.getStatus().getStatusCode() != Status.FAILED) {
				if (logger.isDebugEnabled()) {
					logger.debug("Exception caught while submitting task: ", e);
				}
				throw new ExecutionException(ExecutionContext.getMeaningfulMessage(e), e);
			}
			else {
				return;
			}
		}
	}

	public void submitScheduled(Scheduler scheduler, Task task, VariableStack stack,
			Object constraints) throws ExecutionException {
		setTaskIdentity(stack, task);
		if (logger.isDebugEnabled()) {
			logger.debug(task);
			logger.debug("Submitting task " + task.getIdentity());
		}
		scheduler.addJobStatusListener(this, task);
		setStack(task, stack);
		scheduler.enqueue(task, constraints);
	}

	protected void setTaskIdentity(VariableStack stack, Task task) {
		try {
			task.setIdentity(new IdentityImpl(ThreadingContext.get(stack).toString()));
		}
		catch (VariableNotFoundException e) {
			// such is life
		}
	}

	protected final VariableStack getStack(Task t) {
		synchronized (tasks) {
			return tasks.get(t);
		}
	}

	public void statusChanged(StatusEvent e) {
		try {
			int status = e.getStatus().getStatusCode();
			if (logger.isDebugEnabled()) {
				logger.debug("Task status changed " + e.getSource().getIdentity() + " " + status);
			}
			if (!e.getStatus().isTerminal()) {
				return;
			}
			Task task = (Task) e.getSource();
			VariableStack stack = getStack(task);
			if (stack == null) {
				logger.warn("Received status event from unknown task " + e);
				return;
			}
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Got status event: " + status);
				}
				Scheduler scheduler = getScheduler(stack);
				if (scheduler != null) {
					scheduler.removeJobStatusListener(this, task);
				}
				stack.getExecutionContext().getStateManager().unregisterElement(this, stack);
				removeTask(task);
				if (e.getStatus().getStatusCode() == Status.COMPLETED) {
					taskCompleted(e, stack);
				}
				else if (e.getStatus().getStatusCode() == Status.FAILED) {
					taskFailed(e, stack);
				}
				else if (e.getStatus().getStatusCode() == Status.CANCELED) {
					abort(stack, e.getStatus().getMessage());
				}
			}
			catch (ExecutionException ex) {
				logger.debug("Exception caught while processing status event", ex);
				failImmediately(stack, ex);
			}
			catch (Exception ex) {
				logger.debug("Exception caught while processing status event", ex);
				failImmediately(stack, ex);
			}
		}
		catch (Exception ex) {
			logger.error("Exception caught out of execution context", ex);
		}
	}

	protected void removeTask(Task task) {
		synchronized (tasks) {
			if (tasks.remove(task) == null) {
				logger.warn("Task being removed twice?", new Throwable());
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Tasks in the map: " + tasks.size());
		}
	}

	protected void taskFailed(StatusEvent e, final VariableStack stack) throws ExecutionException {
		if (logger.isDebugEnabled()) {
			Task t = (Task) e.getSource();
			logger.debug("Failed task: " + t.getSpecification() + " on " + t.getAllServices());
		}
		final Exception ex = e.getStatus().getException();
		EventBus.post(new Runnable() {
			public void run() {
				if (ex != null) {
					failImmediately(stack, ex);
				}
				else {
					failImmediately(stack, "Task failed");
				}
			}
		});
	}

	protected void taskCompleted(StatusEvent e, final VariableStack stack)
			throws ExecutionException {
		EventBus.post(new Runnable() {
			public void run() {
				try {
					complete(stack);
				}
				catch (ExecutionException e) {
					failImmediately(stack, e);
				}
			}
		});
	}

	protected void setSecurityContextIfNotLocal(Service service, SecurityContext sc) {
		if (!service.getProvider().equals("local") && service.getSecurityContext() == null) {
			service.setSecurityContext(sc);
		}
	}

	protected SecurityContext getSecurityContext(VariableStack stack, String provider, ServiceContact serviceContact)
			throws InvalidProviderException, ProviderMethodException, ExecutionException {
		if (A_SECURITY_CONTEXT.isPresent(stack)) {
			return (SecurityContext) A_SECURITY_CONTEXT.getValue(stack);
		}
		else if (stack.isDefined("#securitycontext")) {
			return (SecurityContext) this.checkClass(stack.getVar("#securitycontext"),
					SecurityContext.class, "Security Context");
		}
		else {
			return AbstractionFactory.getSecurityContext(provider, serviceContact);
		}
	}
}