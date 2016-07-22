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

package org.globus.cog.karajan.compiled.nodes.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.CleanUpSetImpl;
import org.globus.cog.abstraction.impl.common.StagingSetEntryImpl;
import org.globus.cog.abstraction.impl.common.StagingSetImpl;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.common.task.GenericTaskHandler;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.CleanUpSet;
import org.globus.cog.abstraction.interfaces.EnvironmentVariable;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.StagingSet;
import org.globus.cog.abstraction.interfaces.StagingSetEntry.Mode;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.scheduler.TaskConstraintsImpl;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.TypeUtil;

public class GridExec extends AbstractGridNode {
	private static final Logger logger = Logger.getLogger(GridExec.class);
	
	protected ArgRef<String> executable;
	protected ArgRef<Object> arguments;
	protected ArgRef<Object> host;
	protected ArgRef<String> stdout;
	protected ArgRef<String> stderr;
	protected ArgRef<Number> stdoutLocation;
	protected ArgRef<Number> stderrLocation;
	protected ArgRef<String> stdin;
	protected ArgRef<String> provider;
	protected ArgRef<Number> count;
	protected ArgRef<String> jobType;
	protected ArgRef<Boolean> redirect;
	protected ArgRef<String> directory;
	protected ArgRef<String> nativespec;
	protected ArgRef<Boolean> delegation;
	protected ArgRef<Map<String, Object>> attributes;
	protected ArgRef<Boolean> failOnJobError;
	protected ArgRef<Boolean> batch;
	
	protected ArgRef<List<EnvironmentVariable>> environment;
	protected ChannelRef<List<?>> c_stagein;
	protected ChannelRef<List<?>> c_stageout;
	protected ChannelRef<String> c_cleanup;
	
	protected ChannelRef<Object> cr_vargs;
	protected ChannelRef<Object> cr_stdout;
	protected ChannelRef<Object> cr_stderr;

	@Override
	protected Signature getSignature() {
		return new Signature(
				params(
						"executable", "arguments",
						optional("host", null), 
						optional("stdout", null),
						optional("stderr", null),
						optional("stdoutLocation", null), optional("stderrLocation", null),
						optional("stdin", null),
						optional("provider", null),
						optional("securityContext", null),
						optional("count", null),
						optional("jobType", null),
						optional("redirect", false),
						optional("directory", null),
						optional("nativespec", null),
						optional("delegation", false),
						optional("attributes", Collections.EMPTY_MAP),
						optional("failOnJobError", true),
						optional("batch", false),
						optional("environment", null),
						channel("stagein"),
						channel("stageout"),
						channel("cleanup")
				),
				returns(channel("...", DYNAMIC), channel("stdout", DYNAMIC), channel("stderr", DYNAMIC))
		);
	}

	

	public void submitTask(Stack stack) {
		JobSpecificationImpl js = new JobSpecificationImpl();
		Task task = new TaskImpl();
		
		try {
			js.setExecutable(this.executable.getValue(stack));
			setArguments(js, this.arguments.getValue(stack));
			this.arguments.set(stack, null);

			if (this.redirect.getValue(stack)) {
				js.setStdOutputLocation(FileLocation.MEMORY);
				js.setStdErrorLocation(FileLocation.MEMORY);
			}

			String stmp;
			stmp = this.stdin.getValue(stack);
			if (stmp != null) {
				js.setStdInput(stmp);
			}
			stmp = this.stdout.getValue(stack);
			if (stmp != null) {
				js.setStdOutput(stmp);
			}
			stmp = this.stderr.getValue(stack);
			if (stmp != null) {
				js.setStdError(stmp);
			}
			
			Number stdoutloc = this.stdoutLocation.getValue(stack);
			if (stdoutloc != null) {
				js.setStdOutputLocation(new FileLocation.Impl(stdoutloc.intValue()));
			}
			
			Number stderrloc = this.stderrLocation.getValue(stack);
			if (stderrloc != null) {
				js.setStdErrorLocation(new FileLocation.Impl(stderrloc.intValue()));
			}
			
			stmp = this.directory.getValue(stack);
			if (stmp != null) {
				js.setDirectory(stmp);
			}
			
			stmp = this.jobType.getValue(stack);
			if (stmp != null) {
				js.setAttribute("jobType", stmp);
			}
			
			stmp = this.nativespec.getValue(stack);
			if (stmp != null) {
				js.setSpecification(stmp);
			}
			
			js.setDelegationEnabled(this.delegation.getValue(stack));
			
			Map<String, Object> attributes = this.attributes.getValue(stack);
			if (attributes != null) {
				setAttributes(js, attributes);
			}

			js.setBatchJob(this.batch.getValue(stack));
			setMiscAttributes(js, stack);

			Object host = this.host.getValue(stack);
			String provider = this.provider.getValue(stack);

			addEnvironment(stack, js);
			addStageIn(stack, js);
			addStageOut(stack, js);
			addCleanups(stack, js);

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
					Contact contact = getHost(host, scheduler, provider);
					if (!(contact instanceof BoundContact)) {
						throw new ExecutionException(
								"The host argument cannot be a virtual contact");
					}
					BoundContact bc = (BoundContact) contact;
					Service service = bc.getService(Service.EXECUTION, provider);
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
					Service service = BoundContact.LOCALHOST.getService(Service.EXECUTION, "local");
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
					Contact contact = getHost(host, scheduler, provider);
					if (provider != null) {
						TaskConstraintsImpl tc = new TaskConstraintsImpl();
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
		catch (Exception e) {
			throw new ExecutionException(this, "Exception caught while submitting job", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("TASK_DEFINITION: " + task + " is " + 
					js.getExecutable() + " " + js.getArguments());
		}
	}

	protected void addEnvironment(Stack stack, JobSpecificationImpl js)
			throws ExecutionException {
	    List<EnvironmentVariable> env = this.environment.getValue(stack);
	    if (env != null) {
    		for (EnvironmentVariable e : env) {
    			js.addEnvironmentVariable(e);
    		}
	    }
	}
	
	private static final EnumSet<Mode> DEFAULT_STAGEOUT_MODE = EnumSet.of(Mode.IF_PRESENT, Mode.ON_SUCCESS);
	private static final EnumSet<Mode> DEFAULT_STAGEIN_MODE = EnumSet.of(Mode.ALWAYS);

	protected void addStageIn(Stack stack, JobSpecificationImpl js) {
		js.setStageIn(getStagingSet(stack, c_stagein, DEFAULT_STAGEIN_MODE));
		c_stagein.set(stack, null);
	}
	
	protected void addStageOut(Stack stack, JobSpecificationImpl js) {
        js.setStageOut(getStagingSet(stack, c_stageout, DEFAULT_STAGEOUT_MODE));
        c_stageout.set(stack, null);
    }

	private StagingSet getStagingSet(Stack stack, ChannelRef<List<?>> cref, EnumSet<Mode> mode)
			throws ExecutionException {
		Channel<List<?>> s = cref.get(stack);
		StagingSet ss = new StagingSetImpl();
		for (List<?> e : s) {
			if (e.size() > 2) {
				mode = Mode.fromId(TypeUtil.toInt(e.get(2)));
			}
			ss.add(new StagingSetEntryImpl((String) e.get(0),
						(String) e.get(1), mode));
		}
		return ss;
	}
	
	protected void addCleanups(Stack stack, JobSpecificationImpl js) throws ExecutionException {
	    Channel<String> s = this.c_cleanup.get(stack);
	    if (s == null || s.isEmpty()) {
	        return;
	    }
	    CleanUpSet cs = new CleanUpSetImpl();
	    for (String ss : s) {
	    	cs.add(ss);
	    }
        js.setCleanUpSet(cs);
    }

	@SuppressWarnings("unchecked")
	protected void setArguments(JobSpecification js, Object value) {
		if (value instanceof List) {
			js.setArguments(stringify((List<Object>) value));
		}
		else if (value instanceof Channel) {
			js.setArguments(stringify(((Channel<Object>) value).getAll()));
		}
		else {
			js.setArguments(TypeUtil.toString(value));
		}
	}

	protected void setAttributes(JobSpecification js, Map<String, Object> m) throws ExecutionException {
		for (Map.Entry<String, Object> e : m.entrySet()) {
			try {
				js.setAttribute(e.getKey(), e.getValue());
			}
			catch (ClassCastException ex) {
				throw new ExecutionException("Invalid attribute name (" + e.getKey() + ")", ex);
			}
		}
	}

	protected void setMiscAttributes(JobSpecification js, Stack stack)
			throws ExecutionException {
		setAttributeIfPresent(this.count, "count", js, stack);
		setAttributeIfPresent(this.jobType, "jobType", js, stack);
	}

	protected void setAttributeIfPresent(ArgRef<?> arg, String name, JobSpecification js, Stack stack)
			throws ExecutionException {
		Object value = arg.getValue(stack);
		if (value != null) {
			js.setAttribute(name, value);
		}
	}

	private List<String> stringify(List<Object> l) {
		ArrayList<String> sl = new ArrayList<String>(l.size());
		for (Object o : l) {
			sl.add(String.valueOf(o));
		}
		return sl;
	}

	protected boolean taskFailed(StatusEvent e, Stack stack) throws ExecutionException {
		Task task = (Task) e.getSource();
		returnOutputs(task, stack);
		Exception ex = e.getStatus().getException();
		if (ex instanceof JobException && !this.failOnJobError.getValue(stack)) {
			cr_vargs.append(stack, ((JobException) ex).getExitCode());
			return false;
		}
		else {
			return true;
		}
	}

	protected void taskCompleted(StatusEvent e, Stack stack) throws ExecutionException {
		Task t = (Task) e.getSource();
		returnOutputs(t, stack);
		if (!this.failOnJobError.getValue(stack)) {
			cr_vargs.append(stack, 0);
		}
		super.taskCompleted(e, stack);
	}

	protected void returnOutputs(Task t, Stack stack) throws ExecutionException {
		JobSpecification spec = (JobSpecification) t.getSpecification();
		if (t.getStdOutput() != null && FileLocation.MEMORY.overlaps(spec.getStdOutputLocation())) {
			cr_stdout.append(stack, t.getStdOutput());
		}
		if (t.getStdError() != null && FileLocation.MEMORY.overlaps(spec.getStdErrorLocation())) {
			cr_stderr.append(stack, t.getStdError());
		}
	}
}