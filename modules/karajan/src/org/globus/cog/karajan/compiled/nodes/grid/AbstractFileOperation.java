// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2005
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.CachingFileOperationTaskHandler;
import org.globus.cog.abstraction.impl.common.task.FileOperationSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;

public abstract class AbstractFileOperation extends AbstractGridNode {
	public static final Logger logger = Logger.getLogger(AbstractFileOperation.class);
	
	private ArgRef<String> provider;
	private VarRef<String> cwd;

	@Override
	protected void addLocals(Scope scope) {
		super.addLocals(scope);
		cwd = scope.getVarRef("CWD");
	}

	private static TaskHandler handler;

	public void submitTask(Stack stack) throws ExecutionException {
		Task task = null;
		try {
			String[] arguments = getArguments(stack);
			String op = getOperation(stack);
			
			Scheduler scheduler = getScheduler(stack);
			
			String provider = this.provider.getValue(stack);
			Contact host = getHost(stack, this.host, scheduler, provider);
			
			if (provider == null && BoundContact.LOCALHOST.equals(host)) {
				if (runDirectly(stack, op, arguments, cwd.getValue(stack))) {
					return;
				}
			}
			
			task = new TaskImpl();
			FileOperationSpecification spec = new FileOperationSpecificationImpl();
			spec.setOperation(op);
			
			for (int i = 0; i < arguments.length; i++) {
				spec.addArgument(arguments[i]);
			}
			task.setType(Task.FILE_OPERATION);
			task.setRequiredService(1);
			task.setSpecification(spec);
			
			if (host.equals(BoundContact.LOCALHOST)) {
				spec.setAttribute("cwd", cwd.getValue(stack));
			}
			
			if (scheduler == null) {
				task.setService(0, getService((BoundContact) host, provider));
				SecurityContext sc = getSecurityContext(stack, provider);
				setSecurityContextIfNotLocal(task.getService(0), sc);
				synchronized (this.getClass()) {
					if (handler == null) {
						if (handler == null) {
							handler = new CachingFileOperationTaskHandler();
						}
					}
				}
				submitUnscheduled(handler, task, stack);
			}
			else {
				submitScheduled(scheduler, task, stack, new Contact[] { host });
			}
		}
		catch (TaskException e) {
			throw new ExecutionException(this, e);
		}
		catch (Exception e) {
			throw new ExecutionException(this, e);
		}
	}

	protected boolean runDirectly(Stack stack, String op, String[] arguments, String cwd) {
		return false;
	}

	protected abstract String getOperation(Stack stack) throws TaskException;

	protected abstract String[] getArguments(Stack stack) throws TaskException;

	protected Service getService(BoundContact contact, String provider) {
		if (contact.equals(BoundContact.LOCALHOST)) {
			return contact.getService(Service.FILE_OPERATION, "local");
		}
		else {
			return contact.getService(Service.FILE_OPERATION, provider);
		}
	}
}
