/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 3, 2004
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import java.util.HashMap;
import java.util.Map;

import k.rt.ConditionalYield;
import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Executor;
import k.rt.Stack;
import k.thr.LWThread;

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
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.scheduler.submitQueue.NonBlockingSubmit;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;

public abstract class AbstractGridNode extends InternalFunction {
	private static final Logger logger = Logger.getLogger(AbstractGridNode.class);

	private Map<String, Contact> dynamicHosts;
	
	protected VarRef<Context> context;
	protected ArgRef<Object> host;
	protected ArgRef<SecurityContext> securityContext;

	public AbstractGridNode() {
		dynamicHosts = new HashMap<String, Contact>();
	}

	
	@Override
	protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
			throws CompilationException {
		context = scope.getVarRef("#context");
		return this;
	}

	public final void runBody(LWThread thr) {
		TaskStateFuture tsf = (TaskStateFuture) thr.popState();
		int i = thr.checkSliceAndPopState();
		try {
			switch (i) {
				case 0:
					submitTask(thr.getStack());
				case 1:
					if (tsf != null) {
						tsf.getValue();
					}
			}		
		}
		catch (ConditionalYield y) {
			y.getState().push(y.getFuture());
			throw y;
		}
	}

	protected abstract void submitTask(Stack stack);

	public Scheduler getScheduler(Stack stack) {
	    Context ctx = this.context.getValue(stack);
		return (Scheduler) ctx.getAttribute("TASK:SCHEDULER");
	}
	
	protected Contact getHost(Stack stack, ArgRef<Object> hostarg, Scheduler scheduler, String provider) {
		return getHost(hostarg.getValue(stack), scheduler, provider);
	}

	protected Contact getHost(Object value, Scheduler scheduler, String provider)
			throws ExecutionException {
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
			throw new ExecutionException(this, "The host argument must either be a name of a host known by the "
					+ "current scheduler, or a host object");
		}
	}

	private void addDefaultServices(BoundContact bc, String contact, String provider)
			throws ExecutionException {
		SecurityContext sc = null;
		ServiceContact scc = new ServiceContactImpl(contact);
		if (provider != null) {
			try {
				sc = AbstractionFactory.newSecurityContext(provider, scc);
			}
			catch (Exception e) {
				throw new ExecutionException(this, "Could not get default security context for provider "
						+ provider, e);
			}
		}
		bc.addService(new ServiceImpl(provider, Service.EXECUTION, scc,	sc));
		bc.addService(new ServiceImpl(provider, Service.FILE_OPERATION,scc, sc));
		bc.addService(new ServiceImpl(provider, Service.FILE_TRANSFER, scc, sc));
	}


	public void setSecurityContext(Stack stack, Service service) throws ExecutionException {
		SecurityContext sc = this.securityContext.getValue(stack);
		if (sc != null) {
			service.setSecurityContext(sc);
		}
	}

	public void submitUnscheduled(TaskHandler handler, Task task, Stack stack) throws TaskException {
		setTaskIdentity(stack, task);
		TaskStateFuture tsf = new CustomTaskStateFuture(stack, task, true);
		task.addStatusListener(tsf);
		try {
			new NonBlockingSubmit(handler, task, null).go();
		}
		catch (Exception e) {
			// avoid failing twice if status is already set
			if (task.getStatus().getStatusCode() != Status.FAILED) {
				if (logger.isDebugEnabled()) {
					logger.debug("Exception caught while submitting task: ", e);
				}
				throw new TaskException(Executor.getMeaningfulMessage(e), e);
			}
		}
		throw new ConditionalYield(1, tsf);
	}

	public void submitScheduled(Scheduler scheduler, Task task, Stack stack,
			Object constraints) {
		setTaskIdentity(stack, task);
		if (logger.isDebugEnabled()) {
			logger.debug(task);
			logger.debug("Submitting task " + task.getIdentity());
		}
		TaskStateFuture tsf = new CustomTaskStateFuture(stack, task, false);
		scheduler.enqueue(task, constraints, tsf);
		throw new ConditionalYield(1, tsf);
	}

	protected void setTaskIdentity(Stack stack, Task task) {
		task.setIdentity(new IdentityImpl(LWThread.currentThread().getQualifiedName()));
	}
	
	protected class CustomTaskStateFuture extends TaskStateFuture {
		private final boolean taskHasListener;

		public CustomTaskStateFuture(Stack stack, Task task, boolean taskHasListener) {
			super(stack, task);
			this.taskHasListener = taskHasListener;
		}

		public void statusChanged(StatusEvent e) {
			Stack stack = getStack();
			try {
				int status = e.getStatus().getStatusCode();
				if (logger.isDebugEnabled()) {
					logger.debug("Task status changed " + e.getSource().getIdentity() + " " + status);
				}
				if (!e.getStatus().isTerminal()) {
					return;
				}
				Task task = getTask();
				
			
				if (logger.isDebugEnabled()) {
					logger.debug("Got status event: " + status);
				}
				Scheduler scheduler = getScheduler(stack);
				if (taskHasListener) {
					task.removeStatusListener(this);
				}
				if (e.getStatus().getStatusCode() == Status.COMPLETED) {
					taskCompleted(e, stack);
					setValue(e.getStatus());
				}
				else if (e.getStatus().getStatusCode() == Status.FAILED) {
					if (taskFailed(e, stack)) {
						fail(new ExecutionException(e.getStatus().getMessage(), e.getStatus().getException()));
					}
				}
			}
			catch (Exception ex) {
				logger.debug("Exception caught while processing status event", ex);
				fail(new ExecutionException(AbstractGridNode.this, ex));
			}
		}
	}

	protected void taskCompleted(StatusEvent e, Stack stack) {
	}
	
	protected boolean taskFailed(StatusEvent e, Stack stack) {
		return true;
	}

	protected void setSecurityContextIfNotLocal(Service service, SecurityContext sc) {
		if (!service.getProvider().equals("local") && service.getSecurityContext() == null) {
			service.setSecurityContext(sc);
		}
	}

	protected SecurityContext getSecurityContext(Stack stack, String provider)
			throws InvalidProviderException, ProviderMethodException, ExecutionException {
		SecurityContext sc = null;
		if (this.securityContext != null) {
			sc = this.securityContext.getValue(stack);
		}
		if (sc != null) {
			return sc;
		}
		else {
			return AbstractionFactory.newSecurityContext(provider);
		}
	}
}