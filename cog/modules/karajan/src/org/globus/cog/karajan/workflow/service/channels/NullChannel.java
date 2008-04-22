//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 30, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import org.globus.cog.karajan.workflow.service.UserContext;

public class NullChannel extends AbstractKarajanChannel {

	protected NullChannel() {
		super(null, null);	
	}

	public void sendTaggedData(int i, int flags, byte[] bytes) {
		throw new ChannelIOException("Null channel");
	}

	public UserContext getUserContext() {
		return null;
	}

	public boolean isOffline() {
		return true;
	}
	
	public String toString() {
		return "NullChannel";
	}

	public void start() throws ChannelException {
	}

}
