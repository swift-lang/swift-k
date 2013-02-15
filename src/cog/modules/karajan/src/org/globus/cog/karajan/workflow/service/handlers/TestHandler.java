//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.karajan.workflow.service.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.TestCommand;

public class TestHandler extends RequestHandler {
	private static final Logger logger = Logger.getLogger(TestHandler.class);

	private static Timer timer = new Timer(true);

	private static Map listeners;

	static {
		listeners = new HashMap();
	}

	private static TestCallback callback;

	public static void setCallback(TestCallback callback) {
		TestHandler.callback = callback;
	}
	
	public void requestComplete() throws ProtocolException {
		String mode = new String(getInData(0));
		if ("INITIAL".equals(mode)) {
			final ChannelContext cc = getChannel().getChannelContext();
			sendReply("OK".getBytes());
			timer.schedule(new TimerTask() {
				public void run() {
					TestCommand done = new TestCommand(false);
					KarajanChannel channel = null;
					try {
						channel = ChannelManager.getManager().reserveChannel(cc);
						done.execute(channel);
					}
					catch (Exception e) {
						logger.error("Got exception", e);
					}
					finally {
						ChannelManager.getManager().releaseChannel(channel);
					}
				}
			}, 1000 * 5);
		}
		else {
			callback.done(getChannel());
			sendReply("OK".getBytes());
		}
	}

	public static interface TestCallback {
		void done(KarajanChannel channel);
	}
}
