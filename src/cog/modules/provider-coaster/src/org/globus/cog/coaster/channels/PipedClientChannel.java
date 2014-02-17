//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 30, 2009
 */
package org.globus.cog.coaster.channels;

import org.globus.cog.coaster.RequestManager;

public class PipedClientChannel extends AbstractPipedChannel {

	public PipedClientChannel(RequestManager requestManager, ChannelContext channelContext, PipedServerChannel s) {
		super(requestManager, channelContext, true);
		channelContext.getChannelID().setLocalID(ChannelID.newUID());
		channelContext.getChannelID().setRemoteID(s.getChannelContext().getChannelID().getLocalID());
		s.getChannelContext().getChannelID().setRemoteID(channelContext.getChannelID().getLocalID());
		setOther(s);
		setName(s.getName().replace("spipe", "cpipe"));
	}
}
