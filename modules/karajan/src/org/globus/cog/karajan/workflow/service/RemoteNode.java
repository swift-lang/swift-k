//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 2, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.rmi.server.UID;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.nodes.PartialArgumentsContainer;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.ChannelConfigurationCommand;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.StartCommand;
import org.globus.cog.karajan.workflow.service.commands.StartRemoteGroup;
import org.globus.cog.karajan.workflow.service.commands.UploadInstance;
import org.globus.cog.karajan.workflow.service.commands.VersionCommand;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;

public class RemoteNode extends PartialArgumentsContainer implements Callback {
	public static final Logger logger = Logger.getLogger(RemoteNode.class);

	public static final Arg A_HOST = new Arg.Positional("host");

	public static final String HOST = "##host";
	public static final String SOURCE_HOST = "#sourcecontact";
	public static final String SOURCE_ID = "#sourceid";
	public static final String REMOTE_FLAG = "##remote";
	public static final String CHANNEL = "##channel";
	public static final String COMMANDS = "##commands";

	private final HashMap stacks;

	static {
		setArguments(RemoteNode.class, new Arg[] { A_HOST });
	}

	public RemoteNode() {
		stacks = new HashMap();
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		if (!stack.parentFrame().isDefined(REMOTE_FLAG)) {
			String host = TypeUtil.toString(A_HOST.getValue(stack));
			stack.setVar(HOST, host);
			super.partialArgumentsEvaluated(stack);
			try {
				KarajanChannel channel = ChannelManager.getManager().reserveChannel(host,
						getCredential(stack));
				VersionCommand ver = new VersionCommand();
				register(ver, stack);
				logger.debug("Querying server version");
				ver.executeAsync(channel);
			}
			catch (Exception e) {
				throw new ExecutionException("Could not connect to service on " + host + ":\n\t"
						+ e.getMessage(), e);
			}
		}
		else {
			super.partialArgumentsEvaluated(stack);
			startRest(stack);
		}
	}

	protected synchronized void register(Command cmd, VariableStack stack) {
		cmd.setCallback(this);
		register((Object) cmd, stack);
	}

	protected synchronized void register(Object obj, VariableStack stack) {
		stacks.put(obj, stack);
	}

	protected synchronized VariableStack unregister(Object obj) {
		return (VariableStack) stacks.remove(obj);
	}

	protected synchronized GSSCredential getCredential(VariableStack stack)
			throws ExecutionException {
		try {
			Object sc = stack.getVar("#securityContext");
			if (sc instanceof GSSCredential) {
				return (GSSCredential) sc;
			}
		}
		catch (VariableNotFoundException e) {
		}
		try {
			return new GlobusGSSCredentialImpl(GlobusCredential.getDefaultCredential(),
					GSSCredential.INITIATE_AND_ACCEPT);
		}
		catch (Exception e) {
			throw new ExecutionException("Could not get default credential", e);
		}
	}

	public void replyReceived(Command cmd) {
		VariableStack stack = unregister(cmd);
		KarajanChannel channel = null;
		try {
			String host = stack.getVarAsString(HOST);
			channel = ChannelManager.getManager().reserveChannel(host, getCredential(stack));
			if (cmd instanceof VersionCommand) {
				VersionCommand ver = (VersionCommand) cmd;
				logger.info("Server version: " + ver.getServerVersion());
				InstanceContext ic = channel.getUserContext().newInstanceContext();
				ic.setClientID(new UID().toString());

				UploadInstance ui = new UploadInstance(makeTree(stack), ic,
						(String) getTreeProperty("_filename", this));
				register(ui, stack);

				StartCommand re = new StartCommand(ic, this, stack);
				register(re, stack);

				stack.setVar(CHANNEL, channel);
				ic.setStack(stack);
				ic.setTree(stack.getExecutionContext().getTree());

				StartRemoteGroup srg = new StartRemoteGroup();
				srg.add(ui);
				srg.add(re);
				register(srg, stack);
				srg.executeAsync(channel);
			}
			else if (cmd instanceof UploadInstance) {
				logger.debug("Uploaded");
				UploadInstance ui = (UploadInstance) cmd;
				InstanceContext ic = ui.getInstanceContext();

				ic.setStack(stack);
				ic.setTree(stack.getExecutionContext().getTree());
			}
			else if (cmd instanceof ChannelConfigurationCommand) {
				logger.debug("Channel configured");
			}
			else if (cmd instanceof StartCommand) {
				logger.debug("Remote workflow started");
			}
		}
		catch (Exception e) {
			this.failImmediately(stack, e);
		}
		finally {
			if (channel != null) {
				ChannelManager.getManager().releaseChannel(channel);
			}
		}
	}

	private ElementTree makeTree(VariableStack stack) {
		ElementTree tree = stack.getExecutionContext().getTree().copy();
		tree.setRoot(this);
		return tree;
	}

	public void errorReceived(Command cmd, String msg, Exception t) {
		// TODO unregister all commands
		VariableStack stack = unregister(cmd);
		if (stack == null) {
			logger.error("No stack for command " + cmd);
			return;
		}
		if (t == null) {
			this.failImmediately(stack, "Remote error: " + msg);
		}
		else {
			this.failImmediately(stack, t);
		}

	}

	protected void nonArgChildCompleted(VariableStack stack) throws ExecutionException {
		if (!stack.parentFrame().isDefined(REMOTE_FLAG)) {
			complete(stack);
		}
		else {
			super.nonArgChildCompleted(stack);
		}
	}

	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		if (e.getStack().parentFrame().isDefined(REMOTE_FLAG)) {
			super.notificationEvent(e);
		}
		else if (e instanceof FailureNotificationEvent) {
			e.setFlowElement(this);
			super.notificationEvent(e);
		}
		else {
			super.notificationEvent(e);
		}
	}
}
