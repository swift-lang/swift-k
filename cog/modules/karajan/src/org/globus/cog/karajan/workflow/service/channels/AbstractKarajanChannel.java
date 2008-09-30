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
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.NoSuchHandlerException;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RemoteConfiguration;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.Service;
import org.globus.cog.karajan.workflow.service.RemoteConfiguration.Entry;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public abstract class AbstractKarajanChannel implements KarajanChannel {
	private static final Logger logger = Logger.getLogger(AbstractKarajanChannel.class);
	public static final int DEFAULT_HEARTBEAT_INTERVAL = 5 * 60; //seconds

	private ChannelContext context;
	private volatile int usageCount, longTermUsageCount;
	private RequestManager requestManager;
	private final List registeredMaps;
	private boolean localShutdown, closed;
	private String name;
	private Service callbackService;
	private final boolean client;

	protected AbstractKarajanChannel(RequestManager requestManager, ChannelContext channelContext,
			boolean client) {
		if (channelContext != null) {
			this.context = channelContext;
		}
		else {
			this.context = new ChannelContext();
		}
		this.requestManager = requestManager;
		registeredMaps = new LinkedList();
		this.client = client;
		configureHeartBeat();
	}

	protected void configureHeartBeat() {
	    TimerTask heartBeatTask;
		Entry config = context.getConfiguration();
		int heartBeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
		if (config != null && config.hasOption(RemoteConfiguration.HEARTBEAT)) {
			if (config.hasArg(RemoteConfiguration.HEARTBEAT)) {
				heartBeatInterval = Integer.parseInt(config.getArg(RemoteConfiguration.HEARTBEAT));
			}
			heartBeatInterval *= 1000;
		}
		if (!isOffline() && isClient()) {
		    heartBeatTask = new HeartBeatTask(this);
			context.getTimer().schedule(heartBeatTask, heartBeatInterval, heartBeatInterval);
		}
		else {
			if (logger.isInfoEnabled()) {
				if (config == null) {
					logger.info(this + ": Disabling heartbeats (config is null)");
				}
				else if (!config.hasOption(RemoteConfiguration.HEARTBEAT)) {
					logger.info(this + ": Disabling heartbeats (disabled in config)");
				}
				else if (isOffline()) {
					logger.info(this + ": Disabling heartbeats (offline channel)");
				}
				else if (!isClient()) {
					logger.info(this + ": Disabling heartbeats (not a client)");
				}
			}
		}
		if (!isOffline() && !isClient()) {
		    int mult = 2;
		    heartBeatTask = new HeartBeatCheckTask(this, heartBeatInterval, mult);
			context.getTimer().schedule(heartBeatTask, mult * heartBeatInterval, mult * heartBeatInterval);
		}
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
			logger.debug(this + " REQ>: tag = " + tag + ", fin = " + fin + ", datalen = "
					+ data.length + ", data = " + ppByteBuf(data));
		}
		sendTaggedData(tag, fin ? FINAL_FLAG : 0, data);
	}

	public void sendTaggedReply(int tag, byte[] data, boolean fin, boolean err) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + " REPL>: tag = " + tag + ", fin = " + fin + ", datalen = "
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

	protected int readFromStream(InputStream stream, byte[] buf, int crt) throws IOException {
		int count = stream.read(buf, crt, buf.length - crt);
		if (count == -1) {
			throw new EOFException("Connection closed");
		}
		return crt + count;
	}

	public static void pack(byte[] buf, int offset, int value) {
		buf[offset] = (byte) (value & 0xff);
		buf[offset + 1] = (byte) ((value >> 8) & 0xff);
		buf[offset + 2] = (byte) ((value >> 16) & 0xff);
		buf[offset + 3] = (byte) ((value >> 24) & 0xff);
	}

	public static int unpack(byte[] buf, int offset) {
		int i = 0;
		i += (buf[offset] & 0xff);
		i += (buf[offset + 1] & 0xff) << 8;
		i += (buf[offset + 2] & 0xff) << 16;
		i += (buf[offset + 3] & 0xff) << 24;
		return i;
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

	public void setRequestManager(RequestManager rm) {
		if (rm == null) {
			throw new IllegalArgumentException("The request manager cannot be null");
		}
		this.requestManager = rm;
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
		return client;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return getName();
	}

	public synchronized URI getCallbackURI() throws Exception {
		ensureCallbackServiceStarted();
		return getCallbackService().getContact();
	}

	public Service getCallbackService() {
		return callbackService;
	}

	public void setCallbackService(Service callbackService) {
		this.callbackService = callbackService;
	}

	protected void ensureCallbackServiceStarted() throws Exception {

	}

	protected void handleReply(int tag, boolean fin, boolean error, int len, byte[] data) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + " REPL<: tag = " + tag + ", fin = " + fin + ", err = " + error
					+ ", datalen = " + len + ", data = " + ppByteBuf(data));
		}
		Command cmd = getChannelContext().getRegisteredCommand(tag);
		if (cmd != null) {
			try {
				cmd.replyReceived(data);
				if (fin) {
					if (logger.isInfoEnabled()) {
						logger.info(this + " REPL: " + cmd);
					}
					if (error) {
						cmd.errorReceived();
					}
					else {
						cmd.receiveCompleted();
					}
					unregisterCommand(cmd);
				}
			}
			catch (ProtocolException e) {
				logger.warn("Exception caught while processing reply", e);
				cmd.errorReceived(e.getMessage(), e);
			}
		}
		else {
			unregisteredSender(tag);
		}
	}

	protected void unregisteredSender(int tag) {
		logger.warn(getName() + " Recieved reply to unregistered sender. Tag: " + tag);
	}

	protected void handleRequest(int tag, boolean fin, boolean error, int len, byte[] data) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + " REQ<: tag = " + tag + ", fin = " + fin + ", err = " + error
					+ ", datalen = " + len + ", data = " + ppByteBuf(data));
		}
		RequestHandler handler = getChannelContext().getRegisteredHandler(tag);
		try {
			if (handler != null) {
				handler.register(this);
				handler.dataReceived(data);
			}
			else {
				try {
					handler = getRequestManager().handleInitialRequest(data);
					handler.setId(tag);
					registerHandler(handler, tag);
				}
				catch (NoSuchHandlerException e) {
					logger.warn(getName() + "Could not handle request", e);
				}

			}
			if (fin) {
				try {
					if (logger.isInfoEnabled()) {
						logger.info(this + " REQ: " + handler);
					}
					if (error) {
						handler.errorReceived();
					}
					else {
						handler.receiveCompleted();
					}
				}
				catch (ChannelIOException e) {
					throw e;
				}
				catch (Exception e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Failed to process request", e);
					}
					if (!handler.isReplySent()) {
						handler.sendError(e.toString(), e);
					}
				}
				catch (Error e) {
					if (!handler.isReplySent()) {
						handler.sendError(e.toString(), e);
					}
					throw e;
				}
				finally {
					unregisterHandler(tag);
				}
			}
		}
		catch (ProtocolException e) {
			unregisterHandler(tag);
			logger.warn(e);
		}
	}

	public void flush() throws IOException {
	}
}
