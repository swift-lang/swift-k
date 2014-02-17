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

public class PipedServerChannel extends AbstractPipedChannel {
    
    private static int idSeq = 1;
    
    private static synchronized int nextIdSeq() {
        return idSeq++;
    }
    
	public PipedServerChannel(RequestManager requestManager, ChannelContext channelContext) {
		super(requestManager, channelContext, false);
		channelContext.getChannelID().setLocalID(ChannelID.newUID());
		setName("spipe://" + nextIdSeq());
	}
	
	public void setClientChannel(PipedClientChannel c) {
		setOther(c);
	}
}
