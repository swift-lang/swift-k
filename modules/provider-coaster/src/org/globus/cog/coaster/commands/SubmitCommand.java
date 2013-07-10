//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 29, 2006
 */
package org.globus.cog.coaster.commands;

import java.io.File;
import java.rmi.server.UID;

import org.globus.cog.coaster.ProtocolException;

public class SubmitCommand extends Command {
	private String file, id, username;
	
	public SubmitCommand(String file) {
		super("SUBMIT");
		this.file = file;
		this.username = System.getProperty("user.name");
		this.id = new UID().toString();
	}

	public void send() throws ProtocolException {
		addOutData(username.getBytes());
		addOutData(id.getBytes());
		addOutData(new File(file).getAbsolutePath().getBytes());
		super.send();
	}
}
