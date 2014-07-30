/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 6, 2005
 */
package org.globus.cog.coaster.channels;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.NoSuchHandlerException;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.RemoteConfiguration;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.Service;
import org.globus.cog.coaster.TimeoutException;
import org.globus.cog.coaster.UserContext;
import org.globus.cog.coaster.RemoteConfiguration.Entry;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.handlers.RequestHandler;

public abstract class AbstractCoasterChannel implements CoasterChannel {
	private static final Logger logger = Logger.getLogger(AbstractCoasterChannel.class);
	public static final int DEFAULT_HEARTBEAT_INTERVAL = 30; // seconds
	// some random spread to avoid sending all heartbeats at once
	public static final int DEFAULT_HBI_INITIAL_SPREAD = 10;
	public static final int DEFAULT_HBI_SPREAD = 10;
	
	public static final int TIMEOUT_CHECK_INTERVAL = 1;
	public static final int TIMEOUT = 120;
	
	private ChannelContext context;
	private volatile int usageCount, longTermUsageCount;
	private RequestManager requestManager;
	// private final List registeredMaps;
	private boolean localShutdown, closed;
	private String name;
	private Service callbackService;
	private final boolean client;
	private long lastTime;
	private final Object lastTimeLock = new Object();
	
	private TimerTask timeoutCheckTask;

	protected AbstractCoasterChannel(RequestManager requestManager, ChannelContext channelContext,
			boolean client) {
		if (channelContext != null) {
			this.context = channelContext;
		}
		this.requestManager = requestManager;
		this.client = client;
		configureHeartBeat();
		configureTimeoutChecks();
		updateLastTime();
	}

	protected void configureHeartBeat() {
		Entry config = context.getConfiguration();
		int heartBeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
		if (config != null && config.hasOption(RemoteConfiguration.HEARTBEAT)) {
			if (config.hasArg(RemoteConfiguration.HEARTBEAT)) {
				heartBeatInterval = Integer.parseInt(config.getArg(RemoteConfiguration.HEARTBEAT));
			}
		}
		heartBeatInterval *= 1000;
		
		boolean controlHeartbeats = isClient() == clientControlsHeartbeats();
		
		if (!isOffline() && controlHeartbeats) {
		    scheduleHeartbeats(heartBeatInterval);
		}
		else {
			if (logger.isDebugEnabled()) {
				if (config == null) {
					logger.debug(this + ": Disabling heartbeats (config is null)");
				}
				else if (!config.hasOption(RemoteConfiguration.HEARTBEAT)) {
					logger.debug(this + ": Disabling heartbeats (disabled in config)");
				}
				else if (isOffline()) {
					logger.debug(this + ": Disabling heartbeats (offline channel)");
				}
				else if (!isClient()) {
					logger.debug(this + ": Disabling heartbeats (not a client)");
				}
			}
		}
		if (!isOffline() && !controlHeartbeats) {
			scheduleHeartbeatCheck(heartBeatInterval);
		}
	}
	
	public void scheduleHeartbeats(int heartBeatInterval) {
	    TimerTask heartBeatTask;
	    heartBeatTask = new HeartBeatTask(this);
	    Timer.schedule(heartBeatTask, 
	    		heartBeatInterval + (int) (Math.random() * DEFAULT_HBI_INITIAL_SPREAD * 1000), 
	    		heartBeatInterval + (int) (Math.random() * DEFAULT_HBI_SPREAD * 1000));
	}
	
	public void scheduleHeartbeatCheck(int heartBeatInterval) {
	    TimerTask heartBeatTask;
	    int mult = 2;
	    heartBeatTask = new HeartBeatCheckTask(this, heartBeatInterval, mult);
        Timer.every(mult * heartBeatInterval, heartBeatTask);
	}
	
	public void configureTimeoutChecks() {
		if (logger.isInfoEnabled()) {
			logger.info("Timeout check started for " + this);
		}
		Timer.every(TIMEOUT_CHECK_INTERVAL * 1000, timeoutCheckTask = new TimerTask() {
			public void run() {
			    checkTimeouts();
			}}
		);
	}
	
	protected void checkTimeouts() {
		long now = System.currentTimeMillis();
		long lastTime = getLastTime();
		if (now - lastTime > TIMEOUT * 1000) {
		    TimeoutException e = new TimeoutException(this, "Channel timed out", lastTime);
		    // prevent further timeouts
		    setLastTime(Long.MAX_VALUE);
			context.notifyRegisteredCommandsAndHandlers(e);
			handleChannelException(e);
			timeoutCheckTask.cancel();
		}
	}
	
	protected void updateLastTime() {
		synchronized(lastTimeLock) {
			lastTime = System.currentTimeMillis();
		}
	}
	
	protected long getLastTime() {
		synchronized(lastTimeLock) {
			return lastTime;
	    }
	}
	
	protected void setLastTime(long lastTime) {
        synchronized(lastTimeLock) {
            this.lastTime = lastTime;
        }
    }
	
	protected boolean clientControlsHeartbeats() {
	    return true;
	}

	public void registerCommand(Command cmd) throws ProtocolException {
		context.registerCommand(cmd);
		cmd.setChannel(this);
	}

	public void unregisterCommand(Command cmd) {
	    if (logger.isDebugEnabled()) {
	    	logger.debug("Unregistering " + cmd);
	    }
		context.unregisterCommand(cmd);
	}

	public void registerHandler(RequestHandler handler, int tag) {
		context.registerHandler(handler, tag);
		handler.setChannel(this);
	}

	public void unregisterHandler(int tag) {
		context.unregisterHandler(tag);
	}

	@Override
	public void sendTaggedReply(int tag, byte[] data, boolean fin, boolean err) {
		sendTaggedReply(tag, data, (fin ? FINAL_FLAG : 0) + (err ? ERROR_FLAG : 0));
	}
	
	@Override
	public void sendTaggedReply(int tag, byte[] data, boolean fin, boolean err, SendCallback cb) {
		sendTaggedReply(tag, data, (fin ? FINAL_FLAG : 0) + (err ? ERROR_FLAG : 0), cb);
	}

	public void sendTaggedReply(int tag, byte[] data, boolean fin) {
		sendTaggedReply(tag, data, fin, false);
	}

	public void sendTaggedData(int i, boolean fin, byte[] bytes) {
		sendTaggedData(i, fin, bytes, null);
	}

	public void sendTaggedData(int tag, boolean fin, byte[] data, SendCallback cb) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + " REQ>: tag = " + tag + ", fin = " + fin + ", datalen = "
					+ data.length + ", data = " + ppByteBuf(data));
		}
		sendTaggedData(tag, fin ? FINAL_FLAG : 0, data, cb);
	}

	public void sendTaggedData(int i, int flags, byte[] bytes) {
		sendTaggedData(i, flags, bytes, null);
	}

	public void sendTaggedReply(int tag, byte[] data, int flags) {
		sendTaggedReply(tag, data, flags, null);
	}

	public void sendTaggedReply(int tag, byte[] data, int flags, SendCallback cb) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + " REPL>: tag = " + tag + ", fin = " + flagIsSet(flags, FINAL_FLAG) + ", datalen = "
					+ data.length + ", data = " + ppByteBuf(data));
		}
		
		sendTaggedData(tag, flags | REPLY_FLAG, data, cb);
	}
	
	public void sendTaggedReply(int id, ByteBuffer buf, boolean fin, boolean err, SendCallback cb) {
		sendTaggedReply(id, buf, (fin ? FINAL_FLAG : 0) + (err ? ERROR_FLAG : 0), cb);
	}

	public void sendTaggedReply(int id, ByteBuffer buf, int flags, SendCallback cb) {
		// TODO this should probably be changed to use buffers more efficiently
		if (buf.hasArray() && (buf.limit() == buf.capacity())) {
			sendTaggedReply(id, buf.array(), flags, cb);
		}
		else {
			byte[] bbuf = new byte[buf.limit()];
			buf.get(bbuf);
			buf.rewind();
			sendTaggedReply(id, bbuf, flags, cb);
		}
	}

	public void sendTaggedData(int i, int flags, ByteBuffer buf, SendCallback cb) {
		if (buf.hasArray() && (buf.limit() == buf.capacity())) {
			sendTaggedData(i, flags, buf.array(), cb);
		}
		else {
			byte[] bbuf = new byte[buf.limit()];
			buf.get(bbuf);
			buf.rewind();
			sendTaggedData(i, flags, bbuf, cb);
		}
	}

	public ChannelContext getChannelContext() {
		return context;
	}
	
	public final UserContext getUserContext() {
	    return context.getUserContext();
	}

	public void setChannelContext(ChannelContext context) {
	    if (this.getChannelContext() != null) {
            logger.warn("Changing channel context from " + this.getChannelContext() + " to " + context, new Throwable());
        }
	    else {
	        logger.warn("Setting channel context to " + context, new Throwable());
	    }
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
	
	/**
	   Pretty-print byte buffer
	 */
	public static String ppByteBuf(byte[] data) {
		byte[] buf = new byte[Math.min(data.length, 256)];
		for (int i = 0; i < buf.length; i++) {
			byte b = data[i];
			if (b < 32 && b != 0x0a) {
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
		if (getCallbackService() != null) {
			return getCallbackService().getContact();
		}
		return null;
	}

	public Service getCallbackService() {
		return callbackService;
	}

	public void setCallbackService(Service callbackService) {
		this.callbackService = callbackService;
	}

	protected void ensureCallbackServiceStarted() throws Exception {

	}

	protected void handleReply(int tag, int flags, int len, byte[] data) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + " REPL<: tag = " + tag + ", fin = " + 
					flagIsSet(flags, FINAL_FLAG) + ", err = " + flagIsSet(flags, ERROR_FLAG)
					+ ", datalen = " + len + ", data = " + ppByteBuf(data));
		}
		updateLastTime();
		Command cmd = getChannelContext().getRegisteredCommand(tag);
		if (cmd != null) {
			try {
				boolean fin = finalFlagIsSet(flags);
				boolean err = errorFlagIsSet(flags);
				if (flagIsSet(flags, SIGNAL_FLAG)) {
                    cmd.handleSignal(data);
                }
				else {
					cmd.replyReceived(fin, err, data);
    				if (fin) {
    					if (logger.isDebugEnabled()) {
    						logger.debug(this + " REPL: " + cmd);
    					}
    					if (err) {
    						cmd.errorReceived();
    					}
    					else {
    						cmd.receiveCompleted();
    					}
    					unregisterCommand(cmd);
    				}
				}
			}
			catch (ProtocolException e) {
				logger.warn("Protocol exception caught while processing reply", e);
				cmd.errorReceived(e.getMessage(), e);
			}
			catch (Exception e) {
				logger.warn("Exception caught while processing reply", e);
				cmd.errorReceived(e.getMessage(), e);
			}
		}
		else {
			unregisteredSender(tag);
		}
	}

	protected boolean flagIsSet(int flags, int mask) {
		return (flags & mask) != 0;
	}
	
	protected boolean finalFlagIsSet(int flags) {
		return (flags & FINAL_FLAG) != 0;
	}
	
	protected boolean errorFlagIsSet(int flags) {
		return (flags & ERROR_FLAG) != 0;
	}

	protected void unregisteredSender(int tag) {
		logger.warn(getName() + " Recieved reply to unregistered sender. Tag: " + tag);
	}

	protected void handleRequest(int tag, int flags, int len, byte[] data) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + " REQ<: tag = " + tag + ", fin = " + 
					flagIsSet(flags, FINAL_FLAG) + ", err = " + flagIsSet(flags, ERROR_FLAG)
					+ ", datalen = " + len + ", data = " + ppByteBuf(data));
		}
		updateLastTime();
		RequestHandler handler = getChannelContext().getRegisteredHandler(tag);
		boolean fin = finalFlagIsSet(flags);
		boolean err = errorFlagIsSet(flags);
		try {
		    boolean signal = false;
			if (handler != null) {
				if (flagIsSet(flags, SIGNAL_FLAG)) {
				    signal = true;
					handler.handleSignal(data);
				}
				else {
					handler.dataReceived(fin, err, data);
				}
			}
			else {
				if (flagIsSet(flags, SIGNAL_FLAG)) {
				    /*
				     *  since signals are not part of the normal flow,
				     *  they can arrive after a handler's lifecycle has terminated.
				     *  Since signals are supposed to alter the behavior of a handler,
				     *  but are time-sensitive, the arrival of a signal after
				     *  a handler has sent all the data can have no consequence
				     *  (whether handled or not).
				     *  
				     *  Long story short, signals with no registered handler are fine to discard.
				     */
				    if (logger.isInfoEnabled()) {
				    	logger.info("Got signal for unregistered tag (" + tag + "): " + new String(data));
				    	logger.info("Registered handlers: " + getChannelContext().getActiveHandlers());
				    }
					return;
				}
				try {
					if (flagIsSet(flags, INITIAL_FLAG)) {
						handler = getRequestManager().handleInitialRequest(tag, data);
						handler.setId(tag);
						registerHandler(handler, tag);
					}
					else {
						if (logger.isInfoEnabled()) {
							logger.info("Received spurious request data, tag: " + tag + ", len: " + len);
						}
						return;
					}
				}
				catch (NoSuchHandlerException e) {
					logger.warn(getName() + "Could not handle request", e);
				}

			}
			if (fin && !signal) {
				try {
					if (logger.isDebugEnabled()) {
						logger.debug(this + " REQ: " + handler);
					}
					if (err) {
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
			}
		}
		catch (ProtocolException e) {
			logger.warn(e);
			unregisterHandler(tag);
		}
		catch (Exception e) {
			logger.warn("Unhandled exception in handler processing code (tag: " + tag + ")", e);
			unregisterHandler(tag);
		}
	}

	public void flush() throws IOException {
	}

	@Override
	public SelectableChannel getNIOChannel() {
		return null;
	}

	public synchronized boolean handleChannelException(Exception e) {
        logger.info("Channel config: " + getChannelContext().getConfiguration());
        if (!ChannelManager.getManager().handleChannelException(this, e)) {
            close();
            return false;
        }
        else {
            return true;
        }
    }	
}
