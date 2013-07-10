//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.coaster.handlers;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.ChannelException;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.ShuttingDownException;

public class ShutdownHandler extends RequestHandler {
	private static final Logger logger = Logger.getLogger(ShutdownHandler.class);
	
	public void requestComplete() throws ProtocolException {
		try {
			getChannel().setLocalShutdown();
			ChannelManager.getManager().shutdownChannel(getChannel());
			sendReply("OK".getBytes());
			//getChannel().close();
		}
		catch (ShuttingDownException e) {
			logger.info("Channel is already shutting down", e);
		}
		catch (ChannelException e) {
			logger.warn("Cannot shut down channel", e);
			sendError("Cannot shut down channel");
		}
	}
}
