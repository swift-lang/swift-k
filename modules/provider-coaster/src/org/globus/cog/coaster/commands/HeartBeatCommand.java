//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.coaster.commands;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.HeartBeatHandler;


public class HeartBeatCommand extends Command {
    public static final Logger logger = Logger.getLogger(HeartBeatCommand.class);
    
    private long start;
    private static int sid;
    private int id;
    
	public HeartBeatCommand() {
		super(HeartBeatHandler.NAME);
		id = sid++;
	}
	
	public void send() throws ProtocolException {
	    start = System.currentTimeMillis();
	    addOutData(start);
		super.send();
	}

	public void replyReceived(boolean fin, boolean err, byte[] data) throws ProtocolException {
		super.replyReceived(fin, err, data);
		if (logger.isInfoEnabled()) {
            long rst = getInDataAsLong(0);
            long now = System.currentTimeMillis();
            logger.info(getChannel() + " up latency: " + (now - rst) + "ms, rtt: " + (now - start) + "ms");
        }
	}
}
