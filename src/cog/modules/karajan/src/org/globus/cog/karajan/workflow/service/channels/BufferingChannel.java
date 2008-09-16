//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 6, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.workflow.service.UserContext;

/**
 * Used for buffering of commands with a polling configuration. Which
 * means that reply timeouts should be a channel thing, and should 
 * be considered from the time of the actual send
 */
public class BufferingChannel extends AbstractKarajanChannel implements Purgeable {
	private List buffer;

	public BufferingChannel(ChannelContext channelContext) {
		super(null, channelContext, false);
		buffer = new ArrayList();
	}

	public synchronized void sendTaggedData(int tag, int flags, byte[] data) {
		buffer.add(new DataEntry(tag, flags, data));
	}

	public UserContext getUserContext() {
		return null;
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
	
	public void purge(KarajanChannel channel) throws IOException {
		Iterator i = buffer.iterator();
		while (i.hasNext()) {
			DataEntry de = (DataEntry) i.next();
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
