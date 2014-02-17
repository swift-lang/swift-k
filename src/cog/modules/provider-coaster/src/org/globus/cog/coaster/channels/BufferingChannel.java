//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 6, 2005
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Used for buffering of commands with a polling configuration. Which
 * means that reply timeouts should be a channel thing, and should 
 * be considered from the time of the actual send
 */
public class BufferingChannel extends AbstractCoasterChannel implements Purgeable {
	private List<DataEntry> buffer;

	public BufferingChannel(ChannelContext channelContext) {
		super(null, channelContext, false);
		buffer = new ArrayList<DataEntry>();
	}

	public synchronized void sendTaggedData(int tag, int flags, byte[] data, SendCallback cb) {
		buffer.add(new DataEntry(tag, flags, data));
	}

	public static class DataEntry {
		private final int tag, flags;
		private final byte[] data;

		public DataEntry(int tag, int flags, byte[] data) {
			this.tag = tag;
			this.flags = flags;
			this.data = data;
		}

		public byte[] getData() {
			return data;
		}

		public int getFlags() {
			return flags;
		}

		public int getTag() {
			return tag;
		}		
	}
	
	public void purge(CoasterChannel channel) throws IOException {
	    for (DataEntry de : buffer) {
			channel.sendTaggedData(de.getTag(), de.getFlags(), de.getData());
		}
		buffer.clear();
	}

	public boolean isOffline() {
		return true;
	}
	
	public boolean isStarted() {
		return true;
	}

	public String toString() {
		return "BufferingChannel";
	}

	public void start() throws ChannelException {
	}
}
