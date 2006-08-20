// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2005
 */
package org.globus.cog.karajan.workflow.nodes.grid;

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
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public abstract class AbstractFileOperation extends AbstractGridNode {
	public static final Logger logger = Logger.getLogger(AbstractFileOperation.class);
	
	public static final Arg OA_PROVIDER = new Arg.Optional("provider", "local");
	public static final Arg OA_HOST = new Arg.Optional("host", BoundContact.LOCALHOST);

	private static TaskHandler handler;

	public void submitTask(VariableStack stack) throws ExecutionException {
		Task task = new TaskImpl();
		try {
			FileOperationSpecification spec = new FileOperationSpecificationImpl();
			spec.setOperation(getOperation(stack));
			String[] arguments = getArguments(stack);
			for (int i = 0; i < arguments.length; i++) {
				spec.addArgument(arguments[i]);
			}
			task.setType(Task.FILE_OPERATION);
			task.setRequiredService(1);
			task.setSpecification(spec);
			Scheduler scheduler = getScheduler(stack);
			
			String provider = TypeUtil.toString(OA_PROVIDER.getValue(stack));
			Contact host = getHost(stack, OA_HOST, scheduler, provider);
			
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
		catch (ExecutionException e) {
			logger.warn("Could not execute file operation", e);
			throw e;
		}
		catch (Exception e) {
			if (task.getStatus().getStatusCode() != Status.FAILED) {
				logger.warn("Task handler threw exception but did not set status");
				throw new ExecutionException("Could not sumbit task: " + e.getMessage(), e);
			}
			else {
				// Handled by processing the status event
			}
		}
	}

	protected abstract String getOperation(VariableStack stack) throws ExecutionException;

	protected abstract String[] getArguments(VariableStack stack) throws ExecutionException;

	protected Service getService(BoundContact contact, String provider) throws ExecutionException {
		if (contact.equals(BoundContact.LOCALHOST)) {
			return contact.getService(Service.FILE_OPERATION, "local");
		}
		else {
			return contact.getService(Service.FILE_OPERATION, provider);
		}
	}
}
