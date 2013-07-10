//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.coaster.commands;

import org.globus.cog.coaster.ProtocolException;


public class EchoCommand extends Command {
	private final String data;
	public EchoCommand(String data) {
		super("ECHO");
		this.data = data;
	}
	
	public void send() throws ProtocolException {
		addOutData(data.getBytes());
		super.send();
	}
}
