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

import k.rt.Abort;
import k.rt.Channel;
import k.rt.ConditionalYield;
import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.analyzer.ArgRef;
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

public class Execute extends GridExec {
	public static final Logger logger = Logger.getLogger(Execute.class);
	
	private ArgRef<String> replicationGroup;
	private ArgRef<Channel<Object>> replicationChannel;
	private ArgRef<String> jobid;
	private ArgRef<ProgressState> progress;
	
	private VarRef<Context> context;
	
	@Override
    protected Signature getSignature() {
	    Signature sig = super.getSignature();
	    sig.getParams().add(0, new Param("progress", Param.Type.POSITIONAL));
	    sig.getParams().add(optional("replicationGroup", null));
	    sig.getParams().add(optional("replicationChannel", null));
	    sig.getParams().add(optional("jobid", null));
	    return sig;
    }
	
	@Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        context = scope.getVarRef("#context");
    }

    @Override
    public void submitScheduled(Scheduler scheduler, Task task, Stack stack, Object constraints) {
		try {
			registerReplica(stack, task);
			log(task, stack);
			
			TaskStateFuture tsf = new SwiftTaskStateFuture(stack, task, false);
			scheduler.enqueue(task, constraints, tsf);
			throw new ConditionalYield(1, tsf);
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
	    String jobid = this.jobid.getValue(stack);
	    if (logger.isDebugEnabled()) {
	        logger.debug("jobid=" + jobid + " task=" + task);
	    }
	}

	protected void registerReplica(Stack stack, Task task) throws CanceledReplicaException {
		setTaskIdentity(stack, task);
		
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
    				int c = e.getStatus().getStatusCode();
    				ProgressState ps = progress.getValue(stack);
    				if (c == Status.SUBMITTED) {
    				    ps.setState("Submitted");
    					getReplicationManager(stack).submitted(task, e.getStatus().getTime());
    				}
    				else if (c == Status.STAGE_IN) {
    				    ps.setState("Stage in");
    				}
    				else if (c == Status.STAGE_OUT) {
    				    ps.setState("Stage out");
    				}
    				else if (c == Status.ACTIVE) {
    					ps.setState("Active");
    					getReplicationManager(stack).active(task, e.getStatus().getTime());
    					Execute.this.replicationChannel.getValue(stack).close();
    				}
    				else if (e.getStatus().isTerminal()) {
    				    getReplicationManager(stack).terminated(task);
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
				rm = new ReplicationManager(getScheduler(stack));
				ctx.setAttribute("#replicationManager", rm);
			}
			return rm;
		}
	}
}
