/*
 * Created on Apr 30, 2008
 */
package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;
import org.globus.cog.karajan.workflow.nodes.grid.GridExec;
import org.griphyn.vdl.karajan.lib.replication.CanceledReplicaException;
import org.griphyn.vdl.karajan.lib.replication.ReplicationManager;

public class Execute extends GridExec {
	public static final Logger logger = Logger.getLogger(Execute.class);

	public static final String REPLICATION_MANAGER = "execute:replication-manager";

	public static final Arg A_REPLICATION_GROUP = new Arg.Optional("replicationGroup");
	public static final Arg A_REPLICATION_CHANNEL = new Arg.Optional("replicationChannel");
	public static final Arg A_JOBID = new Arg.Optional("jobid");

	static {
		setArguments(Execute.class, new Arg[] { A_EXECUTABLE, A_ARGS, A_ARGUMENTS, A_HOST,
				A_STDOUT, A_STDERR, A_STDOUTLOCATION, A_STDERRLOCATION, A_STDIN, A_PROVIDER,
				A_COUNT, A_HOST_COUNT, A_JOBTYPE, A_MAXTIME, A_MAXWALLTIME, A_MAXCPUTIME,
				A_ENVIRONMENT, A_QUEUE, A_PROJECT, A_MINMEMORY, A_MAXMEMORY, A_REDIRECT,
				A_SECURITY_CONTEXT, A_DIRECTORY, A_NATIVESPEC, A_DELEGATION, A_ATTRIBUTES,
				C_ENVIRONMENT, A_FAIL_ON_JOB_ERROR, A_BATCH, A_REPLICATION_GROUP,
				A_REPLICATION_CHANNEL, A_JOBID, C_STAGEIN, C_STAGEOUT, C_CLEANUP });
	}

	public Execute() {
	}

	public void submitScheduled(Scheduler scheduler, Task task, VariableStack stack,
			Object constraints) throws ExecutionException {
		try {
			registerReplica(stack, task);
			log(task, stack);
			scheduler.addJobStatusListener(this, task);
			setStack(task, stack);
			scheduler.enqueue(task, constraints);
		}
		catch (CanceledReplicaException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Early abort on replicated task " + task);
			}
			abort(stack);
		}
	}

	void log(Task task, VariableStack stack)
	throws ExecutionException
	{
	    if (logger.isDebugEnabled()) {
	        logger.debug(task);
	        logger.debug("Submitting task " + task);
	    }
	    String jobid = (String) A_JOBID.getValue(stack,null);
	    if (logger.isDebugEnabled()) {
	        logger.debug("jobid="+jobid+" task=" + task);
	    }
	}

	protected void registerReplica(VariableStack stack, Task task) throws CanceledReplicaException {
		setTaskIdentity(stack, task);
		try {
			String rg = TypeUtil.toString(A_REPLICATION_GROUP.getValue(stack, null));
			if (rg != null) {
				getReplicationManager(stack).register(rg, task);
			}
		}
		catch (ExecutionException e) {
			throw new KarajanRuntimeException(e);
		}
	}

	public void statusChanged(StatusEvent e) {
		Task task = (Task) e.getSource();
		VariableStack stack = getStack(task);
		try {
			if (stack != null) {
				int c = e.getStatus().getStatusCode();
				if (c == Status.SUBMITTED) {
					RuntimeStats.setProgress(stack, "Submitted");
					getReplicationManager(stack).submitted(task, e.getStatus().getTime());
				}
				else if (c == Status.STAGE_IN) {
				    RuntimeStats.setProgress(stack, "Stage in");
				}
				else if (c == Status.STAGE_OUT) {
				    RuntimeStats.setProgress(stack, "Stage out");
				}
				else if (c == Status.ACTIVE) {
					RuntimeStats.setProgress(stack, "Active");
					getReplicationManager(stack).active(task, e.getStatus().getTime());
					((FutureVariableArguments) A_REPLICATION_CHANNEL.getValue(stack)).close();
				}
				else if (e.getStatus().isTerminal()) {
				    getReplicationManager(stack).terminated(task);
				}
				else if (c == ReplicationManager.STATUS_NEEDS_REPLICATION) {
					RuntimeStats.setProgress(stack, "Replicating");
					((FutureVariableArguments) A_REPLICATION_CHANNEL.getValue(stack)).append(Boolean.TRUE);
				}
			}
		}
		catch (ExecutionException ex) {
			logger.warn(ex);
		}
		super.statusChanged(e);
	}

	protected ReplicationManager getReplicationManager(VariableStack stack) throws ExecutionException {
		synchronized (stack.firstFrame()) {
			ReplicationManager rm = (ReplicationManager) stack.firstFrame().getVar(
					REPLICATION_MANAGER);
			if (rm == null) {
				rm = new ReplicationManager(getScheduler(stack));
				stack.firstFrame().setVar(REPLICATION_MANAGER, rm);
			}
			return rm;
		}
	}
}
