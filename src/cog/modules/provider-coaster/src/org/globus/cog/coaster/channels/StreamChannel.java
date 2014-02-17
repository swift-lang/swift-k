//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 30, 2009
 */
package org.globus.cog.coaster.channels;

import java.io.InputStream;
import java.io.OutputStream;

import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.UserContext;

public class StreamChannel extends AbstractStreamCoasterChannel {
	private boolean started;	

	public StreamChannel(InputStream is, OutputStream os, RequestManager requestManager,
			ChannelContext channelContext) {
		super(requestManager, channelContext, false);
		setInputStream(is);
		setOutputStream(os);
		channelContext.setUserContext(new UserContext(channelContext));
	}

	protected void reconnect() throws ChannelException {
	}


	public boolean isOffline() {
		return false;
	}

	public boolean isStarted() {
		return started;
	}

	
	public synchronized void start() throws ChannelException {
		if (isClient()) {
			setName("C(local)");
		}
		else {
			setName("S(local)");
		}
		initialize();
		logger.info(getContact() + "Channel started");
		if (isClient()) {
			try {
				configure();
			}
			catch (Exception e) {
				throw new ChannelException("Failed to configure channel", e);
			}
		}
	}

	private void initialize() throws ChannelException {
		ChannelContext context = getChannelContext();
		try {
			register();
			started = true;
		}
		catch (Exception e) {
			logger.debug("Exception while starting channel", e);
			throw new ChannelException(e);
		}
	}

}
