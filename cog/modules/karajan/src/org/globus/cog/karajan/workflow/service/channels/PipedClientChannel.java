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

public class PipedClientChannel extends AbstractPipedChannel {
	public PipedClientChannel(RequestManager requestManager, ChannelContext channelContext, PipedServerChannel s) {
		super(requestManager, channelContext, true);
		setOther(s);
	}
}
