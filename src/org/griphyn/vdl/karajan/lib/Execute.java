/*
 * Copyright 2012 University of Chicagou
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Apr 30, 2008
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import k.rt.Abort;
import k.rt.Channel;
import k.rt.ConditionalYield;
import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.grid.GridExec;
import org.globus.cog.karajan.compiled.nodes.grid.TaskStateFuture;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.griphyn.vdl.karajan.lib.RuntimeStats.ProgressState;
import org.griphyn.vdl.karajan.lib.replication.CanceledReplicaException;
import org.griphyn.vdl.karajan.lib.replication.ReplicationManager;
import org.griphyn.vdl.util.SwiftConfig;

public class Execute extends GridExec {
	public static final Logger logger = Logger.getLogger(Execute.class);
	
	private ArgRef<String> replicationGroup;
	private ArgRef<Channel<Object>> replicationChannel;
	private ArgRef<String> jobid;
	private ArgRef<ProgressState> progress;
	private ArgRef<Map<String, String>> environment;
	
	private VarRef<Context> context;
	
	private boolean replicationEnabled;
	private SwiftConfig config;
	
	@Override
    protected Signature getSignature() {
	    Signature sig = super.getSignature();
	    List<Param> params = sig.getParams();
	    params.add(0, new Param("progress", Param.Type.POSITIONAL));
	    params.add(optional("replicationGroup", null));
	    params.add(optional("replicationChannel", null));
	    params.add(optional("jobid", null));
	    removeParams(params, "stdout", "stderr", "stdoutLocation", "stderrLocation", 
	        "stdin", "provider", "securityContext", "nativespec", 
	        "delegation", "batch");
	    return sig;
    }
	
	private void removeParams(List<Param> params, String... names) {
	    Set<String> snames = new HashSet<String>(Arrays.asList(names));
	    Iterator<Param> i = params.iterator();
	    while (i.hasNext()) {
	        Param p = i.next();
	        if (snames.contains(p.name)) {
	            try {
                    setArg(null, p, new ArgRef.Static<Object>(p.value));
                }
                catch (CompilationException e) {
                    throw new RuntimeException("Failed to remove parameter " + p.name, e);
                }
	            i.remove();
	        }
	    }
    }

    @Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        context = scope.getVarRef("#context");
        config = (SwiftConfig) context.getValue().getAttribute("SWIFT:CONFIG");
        replicationEnabled = config.isReplicationEnabled();
    }

    @Override
    protected void addEnvironment(Stack stack, JobSpecificationImpl js) throws ExecutionException {
        js.setEnvironmentVariables(environment.getValue(stack));
    }

    @Override
    public void submitScheduled(Scheduler scheduler, Task task, Stack stack, Object constraints) {
		try {
		    setTaskIdentity(stack, task);
		    if (replicationEnabled) {
		        registerReplica(stack, task);
		    }
			log(task, stack);
			
			TaskStateFuture tsf = new SwiftTaskStateFuture(stack, task, false);
			scheduler.enqueue(task, constraints, tsf);
			throw new ConditionalYield(1, MAX_STATE, tsf);
		}
		catch (CanceledReplicaException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Early abort on replicated task " + task);
			}
			throw new Abort();
		}
	}

	void log(Task task, Stack stack) throws ExecutionException {
	    if (logger.isDebugEnabled()) {
	        logger.debug(task);
	        logger.debug("Submitting task " + task);
	    }
	    if (logger.isInfoEnabled()) {
	        String jobid = this.jobid.getValue(stack);
	        JobSpecification spec = (JobSpecification) task.getSpecification();
	        logger.info(buildTaskInfoString(task, jobid, spec));
	    }
	}

	private String buildTaskInfoString(Task task, String jobid, JobSpecification spec) {
        StringBuilder sb = new StringBuilder();
        sb.append("JOB_TASK jobid=");
        sb.append(jobid);
        sb.append(" taskid=");
        sb.append(task.getIdentity());
        sb.append(" exec=");
        sb.append(spec.getExecutable());
        sb.append(" dir=");
        sb.append(spec.getDirectory());
        sb.append(" args=");
        sb.append(spec.getArguments());
        return sb.toString();
    }

    protected void registerReplica(Stack stack, Task task) throws CanceledReplicaException {
		String rg = this.replicationGroup.getValue(stack);
		if (rg != null) {
			getReplicationManager(stack).register(rg, task);
		}
	}
	
	protected class SwiftTaskStateFuture extends CustomTaskStateFuture {

        public SwiftTaskStateFuture(Stack stack, Task task, boolean taskHasListener) {
            super(stack, task, taskHasListener);
        }

    	public void statusChanged(StatusEvent e) {
    		Task task = (Task) e.getSource();
    		Stack stack = getStack();
    		try {
    			if (stack != null) {
    			    Status s = e.getStatus();
    				int c = s.getStatusCode();
    				if (logger.isInfoEnabled()) {
    				    if (s.getMessage() == null) {
    				        logger.info("TASK_STATUS_CHANGE taskid=" + e.getSource().getIdentity() + " status=" + c);
    				    }
    				    else {
    				        logger.info("TASK_STATUS_CHANGE taskid=" + e.getSource().getIdentity() + " status=" + c + 
    				            " " + s.getMessage());
    				    }
                    }
    				ProgressState ps = progress.getValue(stack);
    				if (c == Status.SUBMITTED) {
    				    ps.setState("Submitted");
    				    if (replicationEnabled) {
    				        getReplicationManager(stack).submitted(task, s.getTime());
    				    }
    				}
    				else if (c == Status.STAGE_IN) {
    				    ps.setState("Stage in");
    				}
    				else if (c == Status.STAGE_OUT) {
    				    ps.setState("Stage out");
    				}
    				else if (c == Status.ACTIVE) {
    					ps.setState("Active");
    					if (replicationEnabled) {
    					    getReplicationManager(stack).active(task, s.getTime());
    					    Execute.this.replicationChannel.getValue(stack).close();
    					}
    				}
    				else if (e.getStatus().isTerminal()) {
    				    if (replicationEnabled) {
    				        getReplicationManager(stack).terminated(task);
    				    }
    				}
    				else if (c == ReplicationManager.STATUS_NEEDS_REPLICATION) {
    					ps.setState("Replicating");
    					Execute.this.replicationChannel.getValue(stack).add(Boolean.TRUE);
    				}
    			}
    		}
    		catch (ExecutionException ex) {
    			logger.warn(ex);
    		}
    		super.statusChanged(e);
    	}
	}

	protected ReplicationManager getReplicationManager(Stack stack) throws ExecutionException {
	    Context ctx = this.context.getValue(stack);
		synchronized (ctx) {
			ReplicationManager rm = (ReplicationManager) ctx.getAttribute("#replicationManager");
			if (rm == null) {
				rm = new ReplicationManager(getScheduler(stack), config);
				ctx.setAttribute("#replicationManager", rm);
			}
			return rm;
		}
	}
}
