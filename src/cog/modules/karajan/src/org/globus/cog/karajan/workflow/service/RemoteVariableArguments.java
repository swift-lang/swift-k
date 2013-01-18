//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 24, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.arguments.AbstractWriteOnlyVariableArguments;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.VargCommand;

public class RemoteVariableArguments extends AbstractWriteOnlyVariableArguments {
	private final Arg.Channel channel;
	private final InstanceContext ic;

	public RemoteVariableArguments(Arg.Channel channel, InstanceContext ic) {
		this.channel = channel;
		this.ic = ic;
	}

	public void merge(VariableArguments args) {
		appendAll(args.getAll());
	}

	public synchronized void append(Object value) {
		try {
			KarajanChannel channel = ChannelManager.getManager().reserveChannel(
					ic.getChannelContext());
			VargCommand cmd = new VargCommand(ic, this.channel.getName(), value);
			// TODO handle replies
			cmd.executeAsync(channel);
			ChannelManager.getManager().releaseChannel(channel);
		}
		catch (Exception e) {
			throw new KarajanRuntimeException(e);
		}
	}

	public void appendAll(List args) {
		Iterator i = args.iterator();
		while (i.hasNext()) {
			append(i.next());
		}
	}

	public boolean isCommutative() {
		return false;
	}

	
}
