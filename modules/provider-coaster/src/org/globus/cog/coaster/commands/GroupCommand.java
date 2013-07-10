//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.coaster.commands;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.CoasterChannel;

public abstract class GroupCommand extends Command {
	private final List<Command> members;

	public GroupCommand(String cmd) {
		super(cmd);
		members = new LinkedList<Command>();
	}

	public void add(Command cmd) {
		members.add(cmd);
	}

	public void executeAsync(CoasterChannel channel) throws ProtocolException {
		channel.registerCommand(this);
		byte[] btags = new byte[members.size() * 4];
		ByteBuffer tags = ByteBuffer.wrap(btags);
		for (Command cmd : members) {
			channel.registerCommand(cmd);
			tags.putInt(cmd.getId());
		}
		addOutData(btags);
		send();
		
		for (Command cmd : members) {
			cmd.send();
		}
	}
}
