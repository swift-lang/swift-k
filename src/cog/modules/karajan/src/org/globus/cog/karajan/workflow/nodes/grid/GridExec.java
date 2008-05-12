// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.common.task.GenericTaskHandler;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.scheduler.TaskConstraints;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class GridExec extends AbstractGridNode implements StatusListener {
	private static final Logger logger = Logger.getLogger(GridExec.class);

	public static final Arg A_EXECUTABLE = new Arg.Positional("executable");
	public static final Arg A_ARGS = new Arg.Positional("args");
	public static final Arg A_ARGUMENTS = new Arg.Optional("arguments", "");
	public static final Arg A_HOST = new Arg.Optional("host");
	public static final Arg A_STDOUT = new Arg.Optional("stdout");
	public static final Arg A_STDERR = new Arg.Optional("stderr");
	public static final Arg A_STDOUTLOCATION = new Arg.Optional("stdoutLocation");
	public static final Arg A_STDERRLOCATION = new Arg.Optional("stderrLocation");
	public static final Arg A_STDIN = new Arg.Optional("stdin");
	public static final Arg A_PROVIDER = new Arg.Optional("provider");
	public static final Arg A_SECURITY_CONTEXT = new Arg.Optional("securitycontext");
	public static final Arg A_COUNT = new Arg.Optional("count");
	public static final Arg A_HOST_COUNT = new Arg.Optional("hostcount");
	public static final Arg A_JOBTYPE = new Arg.Optional("jobtype");
	public static final Arg A_MAXTIME = new Arg.Optional("maxtime");
	public static final Arg A_MAXWALLTIME = new Arg.Optional("maxwalltime");
	public static final Arg A_MAXCPUTIME = new Arg.Optional("maxcputime");
	public static final Arg A_ENVIRONMENT = new Arg.Optional("environment");
	public static final Arg A_QUEUE = new Arg.Optional("queue");
	public static final Arg A_PROJECT = new Arg.Optional("project");
	public static final Arg A_MINMEMORY = new Arg.Optional("minmemory");
	public static final Arg A_MAXMEMORY = new Arg.Optional("maxmemory");
	public static final Arg A_REDIRECT = new Arg.Optional("redirect", Boolean.FALSE);
	public static final Arg A_DIRECTORY = new Arg.Optional("directory");
	public static final Arg A_NATIVESPEC = new Arg.Optional("nativespec");
	public static final Arg A_DELEGATION = new Arg.Optional("delegation", Boolean.FALSE);
	public static final Arg.Channel C_ENVIRONMENT = new Arg.Channel("environment");
	public static final Arg A_ATTRIBUTES = new Arg.Optional("attributes", Collections.EMPTY_MAP);
	public static final Arg A_FAIL_ON_JOB_ERROR = new Arg.Optional("failonjoberror", Boolean.TRUE);
	public static final Arg A_BATCH = new Arg.Optional("batch", Boolean.FALSE);

	static {
		setArguments(GridExec.class, new Arg[] { A_EXECUTABLE, A_ARGS, A_ARGUMENTS, A_HOST,
				A_STDOUT, A_STDERR, A_STDOUTLOCATION, A_STDERRLOCATION, A_STDIN, A_PROVIDER,
				A_COUNT, A_HOST_COUNT, A_JOBTYPE, A_MAXTIME, A_MAXWALLTIME, A_MAXCPUTIME,
				A_ENVIRONMENT, A_QUEUE, A_PROJECT, A_MINMEMORY, A_MAXMEMORY, A_REDIRECT,
				A_SECURITY_CONTEXT, A_DIRECTORY, A_NATIVESPEC, A_DELEGATION, A_ATTRIBUTES,
				C_ENVIRONMENT, A_FAIL_ON_JOB_ERROR, A_BATCH });
	}

	public void submitTask(VariableStack stack) throws ExecutionException {
		try {
			JobSpecificationImpl js = new JobSpecificationImpl();
			Task task = new TaskImpl();

			js.setExecutable(TypeUtil.toString(A_EXECUTABLE.getValue(stack)));
			if (A_ARGUMENTS.isPresent(stack)) {
				setArguments(js, A_ARGUMENTS.getValue(stack));
			}
			else if (A_ARGS.isPresent(stack)) {
				setArguments(js, A_ARGS.getValue(stack));
			}
			
			if (TypeUtil.toBoolean(A_REDIRECT.getValue(stack))) {
				js.setStdOutputLocation(FileLocation.MEMORY);
				js.setStdErrorLocation(FileLocation.MEMORY);
			}

			String stmp;
			stmp = TypeUtil.toString(A_STDIN.getValue(stack, null));
			if (stmp != null) {
				js.setStdInput(stmp);
			}
			stmp = TypeUtil.toString(A_STDOUT.getValue(stack, null));
			if (stmp != null) {
				js.setStdOutput(stmp);
			}
			stmp = TypeUtil.toString(A_STDERR.getValue(stack, null));
			if (stmp != null) {
				js.setStdError(stmp);
			}
			if (A_STDOUTLOCATION.isPresent(stack)) {
				js.setStdOutputLocation(new FileLocation.Impl(
						TypeUtil.toInt(A_STDOUTLOCATION.getValue(stack))));
			}
			if (A_STDERRLOCATION.isPresent(stack)) {
				js.setStdErrorLocation(new FileLocation.Impl(
						TypeUtil.toInt(A_STDERRLOCATION.getValue(stack))));
			}
			stmp = TypeUtil.toString(A_DIRECTORY.getValue(stack, null));
			if (stmp != null) {
				js.setDirectory(stmp);
			}
			stmp = TypeUtil.toString(A_JOBTYPE.getValue(stack, null));
			if (stmp != null) {
				js.setAttribute("jobType", stmp);
			}
			stmp = TypeUtil.toString(A_NATIVESPEC.getValue(stack, null));
			if (stmp != null) {
				js.setSpecification(stmp);
			}
			js.setDelegationEnabled(TypeUtil.toBoolean(A_DELEGATION.getValue(stack)));
			if (A_ENVIRONMENT.isPresent(stack)) {
				setEnvironment(js, A_ENVIRONMENT.getValue(stack));
			}
			if (A_ATTRIBUTES.isPresent(stack)) {
				setAttributes(js, A_ATTRIBUTES.getValue(stack));
			}
			if (A_ATTRIBUTES.isPresent(stack)) {
				setAttributes(js, A_ATTRIBUTES.getValue(stack));
			}
			js.setBatchJob(TypeUtil.toBoolean(A_BATCH.getValue(stack)));
			setMiscAttributes(js, stack);

			Object host = A_HOST.getValue(stack, null);
			String provider = TypeUtil.toString(A_PROVIDER.getValue(stack, null));

			VariableArguments env = C_ENVIRONMENT.get(stack);
			Iterator j = env.iterator();
			while (j.hasNext()) {
				Object v = j.next();
				try {
					Map.Entry e = (Map.Entry) v;
					js.addEnvironmentVariable((String) e.getKey(), (String) e.getValue());
				}
				catch (ClassCastException e) {
					throw new ExecutionException("Invalid environment entry: " + v);
				}
			}
			if (js.getArguments() == null && js.getSpecification() == null) {
				js.setArguments("");
			}
			if (js.getSpecification() == null && js.getExecutable() == null) {
				throw new ExecutionException("No executable or specification provided");
			}
			task.setType(Task.JOB_SUBMISSION);
			task.setRequiredService(1);
			task.setSpecification(js);
			Scheduler scheduler = getScheduler(stack);
			if (scheduler == null) {
				TaskHandler handler = new GenericTaskHandler();
				if (provider == null) {
					throw new ExecutionException("No scheduler defined and no provider specified");
				}
				if (host != null) {
					Contact contact = getHost(stack, A_HOST, scheduler, provider);
					if (!(contact instanceof BoundContact)) {
						throw new ExecutionException(
								"The host argument cannot be a virtual contact");
					}
					BoundContact bc = (BoundContact) contact;
					Service service = bc.getService(Service.JOB_SUBMISSION, provider);
					if (service == null) {
						throw new ExecutionException("Invalid provider: the host " + contact
								+ " does not have a " + provider + " service");
					}
					String project = (String) service.getAttribute("project");
					if (project != null) {
						js.setAttribute("project", project);
					}
					setSecurityContext(stack, service);
					task.setService(0, service);
				}
				else if ("local".equals(provider)) {
					Service service = BoundContact.LOCALHOST.getService(Service.JOB_SUBMISSION,
							"local");
					setSecurityContext(stack, service);
					task.setService(0, service);
				}
				else {
					throw new ExecutionException("No scheduler defined and no host specified");
				}
				submitUnscheduled(handler, task, stack);
			}
			else {
				if (host != null) {
					Contact contact = getHost(stack, A_HOST, scheduler, provider);
					if (provider != null) {
						TaskConstraints tc = new TaskConstraints();
						tc.addConstraint("provider", provider);
						contact.setConstraints(tc);
					}
					submitScheduled(scheduler, task, stack, new Contact[] { contact });
				}
				else {
					submitScheduled(scheduler, task, stack, null);
				}
			}
		}
		catch (ExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ExecutionException("Exception caught while submitting job", e);
		}
	}

	protected void setArguments(JobSpecification js, Object value) {
		if (value instanceof List) {
			js.setArguments(stringify((List) value));
		}
		else if (value instanceof VariableArguments) {
			js.setArguments(stringify(((VariableArguments) value).getAll()));
		}
		else {
			js.setArguments(TypeUtil.toString(value));
		}
	}

	protected void setEnvironment(JobSpecification js, Object value) throws ExecutionException {
		if (value instanceof Map) {
			Iterator j = ((Map) value).entrySet().iterator();
			while (j.hasNext()) {
				Map.Entry e = (Map.Entry) j.next();
				js.addEnvironmentVariable((String) e.getKey(), (String) e.getValue());
			}
		}
		else if (value instanceof String) {
			StringTokenizer st = new StringTokenizer((String) value, ",");
			while (st.hasMoreTokens()) {
				String el = st.nextToken().trim();
				String[] nv = el.split("=");
				if (nv.length > 2 || el.length() == 0) {
					throw new ExecutionException("Invalid environment entry: '" + el + "'");
				}
				else if (nv.length == 1) {
					js.addEnvironmentVariable(nv[0].trim(), "");
				}
				else {
					js.addEnvironmentVariable(nv[0].trim(), nv[1].trim());
				}
			}
		}
	}

	protected void setAttributes(JobSpecification js, Object value) throws ExecutionException {
		Map m = (Map) checkClass(value, Map.class, "map");
		Iterator ai = m.entrySet().iterator();
		while (ai.hasNext()) {
			Map.Entry e = (Map.Entry) ai.next();
			try {
				js.setAttribute((String) e.getKey(), e.getValue());
			}
			catch (ClassCastException ex) {
				throw new ExecutionException("Invalid attribute name (" + e.getKey() + ")", ex);
			}
		}
	}

	protected final static Arg[] MISC_ATTRS = new Arg[] { A_COUNT, A_HOST_COUNT, A_JOBTYPE,
			A_MAXTIME, A_MAXWALLTIME, A_MAXCPUTIME, A_QUEUE, A_PROJECT, A_MINMEMORY, A_MAXMEMORY };

	protected void setMiscAttributes(JobSpecification js, VariableStack stack) {
		for (int i = 0; i < MISC_ATTRS.length; i++) {
			setAttributeIfPresent(MISC_ATTRS[i], js, stack);
		}
	}
	
	protected void setAttributeIfPresent(Arg arg, JobSpecification js, VariableStack stack) {
		
	}

	private List stringify(List l) {
		ArrayList sl = new ArrayList(l.size());
		Iterator i = l.iterator();
		while (i.hasNext()) {
			sl.add(TypeUtil.toString(i.next()));
		}
		return sl;
	}

	protected void taskFailed(StatusEvent e, VariableStack stack) throws ExecutionException {
		Task task = (Task) e.getSource();
		returnOutputs(task, stack);
		Exception ex = e.getStatus().getException();
		if (ex instanceof JobException && !TypeUtil.toBoolean(A_FAIL_ON_JOB_ERROR.getValue(stack))) {
			Arg.VARGS.ret(stack, new Integer(((JobException) ex).getExitCode()));
			super.taskCompleted(e, stack);
		}
		else {
			if (e.getStatus().getException() != null) {
				failImmediately(stack, e.getStatus().getException());
			}
			else if (e.getStatus().getMessage() != null) {
				failImmediately(stack, e.getStatus().getMessage());
			}
			else {
				failImmediately(stack, "Task failed");
			}
		}
	}

	protected void taskCompleted(StatusEvent e, VariableStack stack) throws ExecutionException {
		Task t = (Task) e.getSource();
		returnOutputs(t, stack);
		if (!TypeUtil.toBoolean(A_FAIL_ON_JOB_ERROR.getValue(stack))) {
			Arg.VARGS.ret(stack, new Integer(0));
		}
		super.taskCompleted(e, stack);
	}

	protected void returnOutputs(Task t, VariableStack stack) throws ExecutionException {
		JobSpecification spec = (JobSpecification) t.getSpecification();
		if (t.getStdOutput() != null && FileLocation.MEMORY.overlaps(spec.getStdOutputLocation())) {
			STDOUT.ret(stack, t.getStdOutput());
		}
		if (t.getStdError() != null && FileLocation.MEMORY.overlaps(spec.getStdErrorLocation())) {
			STDERR.ret(stack, t.getStdError());
		}
	}
}