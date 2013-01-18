//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.karajan.workflow.service.commands;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public abstract class GroupCommand extends Command {
	private final List members;

	public GroupCommand(String cmd) {
		super(cmd);
		members = new LinkedList();
	}

	public void add(Command cmd) {
		members.add(cmd);
	}

	public void executeAsync(KarajanChannel channel) throws ProtocolException {
		channel.registerCommand(this);
		byte[] btags = new byte[members.size() * 4];
		ByteBuffer tags = ByteBuffer.wrap(btags);
		Iterator i = members.iterator();
		while (i.hasNext()) {
			Command cmd = (Command) i.next();
			channel.registerCommand(cmd);
			tags.putInt(cmd.getId());
		}
		addOutData(btags);
		send();
		
		i = members.iterator();
		while (i.hasNext()) {
			Command cmd = (Command) i.next();
			cmd.send();
		}
	}
}
