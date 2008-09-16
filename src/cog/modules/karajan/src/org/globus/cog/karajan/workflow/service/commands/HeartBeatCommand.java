//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.karajan.workflow.service.commands;

import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.handlers.HeartBeatHandler;


public class HeartBeatCommand extends Command {
    private long start;
    private static int sid;
    private int id;
    
	public HeartBeatCommand() {
		super(HeartBeatHandler.NAME);
		id = sid++;
	}
	
	public void send() throws ProtocolException {
	    start = System.currentTimeMillis();
	    addOutData(String.valueOf(start));
		super.send();
	}

	public void replyReceived(byte[] data) throws ProtocolException {
		super.replyReceived(data);
	}
}
