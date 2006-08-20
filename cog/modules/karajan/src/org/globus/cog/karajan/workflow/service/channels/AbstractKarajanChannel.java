//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 6, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public abstract class AbstractKarajanChannel implements KarajanChannel {
	private static final Logger logger = Logger.getLogger(AbstractKarajanChannel.class);

	private ChannelContext context;
	private volatile int usageCount, longTermUsageCount;
	private final RequestManager requestManager;
	private final List registeredMaps;
	private boolean localShutdown, closed;

	protected AbstractKarajanChannel(RequestManager requestManager, ChannelContext channelContext) {
		if (channelContext != null) {
			this.context = channelContext;
		}
		else {
			this.context = new ChannelContext();
		}
		this.requestManager = requestManager;
		registeredMaps = new LinkedList();
	}

	public void registerCommand(Command cmd) throws ProtocolException {
		context.registerCommand(cmd);
		cmd.register(this);
	}

	public void unregisterCommand(Command cmd) {
		context.unregisterCommand(cmd);
	}

	public void registerHandler(RequestHandler handler, int tag) {
		context.registerHandler(handler, tag);
		handler.register(this);
	}

	public void unregisterHandler(int tag) {
		context.unregisterHandler(tag);
	}

	public void sendTaggedReply(int tag, byte[] data, boolean fin) {
		sendTaggedReply(tag, data, fin, false);
	}

	public void sendTaggedData(int tag, boolean fin, byte[] data) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + "REQ>: tag = " + tag + ", fin = " + fin + ", datalen = "
					+ data.length + ", data = " + ppByteBuf(data));
		}
		sendTaggedData(tag, fin ? FINAL_FLAG : 0, data);
	}

	public void sendTaggedReply(int tag, byte[] data, boolean fin, boolean err) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + "REPL>: tag = " + tag + ", fin = " + fin + ", datalen = "
					+ data.length + ", data = " + ppByteBuf(data));
		}
		int flags = REPLY_FLAG;
		if (fin) {
			flags |= FINAL_FLAG;
		}
		if (err) {
			flags |= ERROR_FLAG;
		}
		sendTaggedData(tag, flags, data);
	}

	public ChannelContext getChannelContext() {
		return context;
	}
	
	public void setChannelContext(ChannelContext context) {
		this.context = context;
	}
	
	protected void readFromStream(InputStream stream, ByteBuffer buf) throws IOException {
		int count = stream.read(buf.array());
		if (count == -1) {
			throw new EOFException("Connection closed");
		}
		buf.position(buf.position() + count);
	}

	protected int readFromStream(InputStream stream, byte[] buf, int crt) throws IOException {
		int count = stream.read(buf, crt, buf.length - crt);
		if (count == -1) {
			throw new EOFException("Connection closed");
		}
		return crt + count;
	}
	
	public static String ppByteBuf(byte[] data) {
		byte[] buf = new byte[Math.min(data.length, 256)];
		for (int i = 0; i < buf.length; i++) {
			byte b = data[i];
			if (b < 32 && b != 0x0a) {
				b = '.';
			}
			else if (b > 128) {
				b = '.';
			}
			buf[i] = b;
		}
		return new String(buf);
	}

	public RequestManager getRequestManager() {
		return requestManager;
	}

	public int decUsageCount() {
		return --usageCount;
	}

	public int incUsageCount() {
		return ++usageCount;
	}

	public int decLongTermUsageCount() {
		return --longTermUsageCount;
	}

	public int incLongTermUsageCount() {
		return ++longTermUsageCount;
	}

	protected int getLongTermUsageCount() {
		return longTermUsageCount;
	}

	public void shutdown() {
	}

	public void close() {
		closed = true;
	}
	
	public boolean isClosed() {
		return closed;
	}

	public void setLocalShutdown() {
		this.localShutdown = true;
	}

	public boolean isLocalShutdown() {
		return localShutdown;
	}

	public boolean isClient() {
		return false;
	}
}
