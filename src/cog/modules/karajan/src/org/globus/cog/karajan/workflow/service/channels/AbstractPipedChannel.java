//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 30, 2009
 */
package org.globus.cog.karajan.workflow.service.channels;

import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.UserContext;

public class AbstractPipedChannel extends AbstractKarajanChannel {
	private UserContext uc;
	private AbstractPipedChannel s;
	
	public AbstractPipedChannel(RequestManager requestManager, ChannelContext channelContext, boolean client) {
		super(requestManager, channelContext, client);
		uc = new UserContext(null, channelContext);
	}
	
	protected void setOther(AbstractPipedChannel s) {
		this.s = s;
	}
	
	protected void configureHeartBeat() {
		//no heart beat for these
	}

	public UserContext getUserContext() {
		return uc;
	}

	public boolean isOffline() {
		return false;
	}

	public boolean isStarted() {
		return true;
	}

	public void sendTaggedData(int tag, int flags, byte[] bytes, SendCallback cb) {
		if (s == null) {
			throw new IllegalStateException("No endpoint set");
		}
		boolean fin = (flags & FINAL_FLAG) != 0;
		boolean error = (flags & ERROR_FLAG) != 0;
		boolean reply = (flags & REPLY_FLAG) != 0;
		if (reply) {
			s.handleReply(tag, fin, error, bytes.length, bytes);
		}
		else {
			s.handleRequest(tag, fin, error, bytes.length, bytes);
		}
		if (cb != null) {
			cb.dataSent();
		}
	}

	public void start() throws ChannelException {
	}
}
