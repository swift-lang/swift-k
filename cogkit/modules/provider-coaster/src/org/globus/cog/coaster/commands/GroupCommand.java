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
