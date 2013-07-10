//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 30, 2005
 */
package org.globus.cog.coaster.channels;


public class NullChannel extends AbstractCoasterChannel {
	private boolean sink;

	protected NullChannel() {
		super(null, null, false);
	}
	
	protected NullChannel(boolean sink) {
        super(null, null, false);
        this.sink = sink;
    }
	
	protected void configureHeartBeat() {
		// override to do nothing
	}
	
	public void configureTimeoutChecks() {
		// do nothing
	}

	public void sendTaggedData(int i, int flags, byte[] bytes, SendCallback cb) {
		if (!sink) {
			throw new ChannelIOException("Null channel");
		}
	}

	public boolean isOffline() {
		return true;
	}
	
	public String toString() {
		return "NullChannel";
	}

	public void start() throws ChannelException {
	}

	public boolean isStarted() {
		return true;
	}
}
