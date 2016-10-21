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
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.NoSuchHandlerException;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.RequestReply;
import org.globus.cog.coaster.Service;
import org.globus.cog.coaster.TimeoutException;
import org.globus.cog.coaster.UserContext;
import org.globus.cog.coaster.channels.ChannelOptions.Type;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.handlers.RequestHandler;

public abstract class AbstractCoasterChannel implements CoasterChannel {
    private static final UserContext DEFAULT_USER_CONTEXT = new UserContext();
    
	private static final Logger logger = Logger.getLogger(AbstractCoasterChannel.class);
	public static final int DEFAULT_HEARTBEAT_INTERVAL = 30; // seconds
	// some random spread to avoid sending all heartbeats at once
	public static final int DEFAULT_HBI_INITIAL_SPREAD = 10;
	public static final int DEFAULT_HBI_SPREAD = 10;
	
	public static final int TIMEOUT_CHECK_INTERVAL = 1;
	// Blue Waters increase
	public static final int TIMEOUT = 300;
	
	private final UserContext userContext;
	private RequestManager requestManager;
	// private final List registeredMaps;
	private boolean localShutdown, closed;
	private String name, id;
	private boolean client;
	private long lastTime;
	private long lastHeartBeat;
	
	private final Object lastTimeLock = new Object();
	private int cmdseq;
	private TagTable<Command> activeSenders;
    private TagTable<RequestHandler> activeReceivers;
	
	private TimerTask timeoutCheckTask;
	
	private List<ChannelListener> listeners;
	
	private Service service;
	private static int idseq;

	protected AbstractCoasterChannel(RequestManager requestManager, UserContext userContext, boolean client) {
		if (userContext != null) {
			this.userContext = userContext;
		}
		else {
		    this.userContext = DEFAULT_USER_CONTEXT;
		}
		this.requestManager = requestManager;
		this.client = client;
		activeSenders = new TagTable<Command>();
        activeReceivers = new TagTable<RequestHandler>();
		synchronized(AbstractCoasterChannel.class) {
		    id = String.valueOf(++idseq);
		}
	}
	
	public String getID() {
        return id;
    }
	
	public void start() throws ChannelException {
	    configureHeartBeat();
        configureTimeoutChecks();
        updateLastTime();
	}


    protected void configureHeartBeat() {
		int heartBeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
		heartBeatInterval *= 1000;
		
		boolean controlHeartbeats = isClient() == clientControlsHeartbeats();
		
		if (controlHeartbeats) {
		    scheduleHeartbeats(heartBeatInterval);
		}
		else if (!isClient()) {
			if (logger.isDebugEnabled()) {
			    logger.debug(this + ": Disabling heartbeats (not a client)");
			}
		}
		if (!controlHeartbeats) {
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
	
	@Override
	public void registerCommand(Command cmd) throws ProtocolException {
        if (cmd.getId() == RequestReply.NOID) {
            cmd.setId(nextCmdSeq());
            synchronized(activeSenders) {
                activeSenders.put(cmd.getId(), cmd);
            }
            cmd.setChannel(this);
        }
        else {
            throw new ProtocolException("Command already registered with id " + cmd.getId());
        }
    }
	
	public synchronized int nextCmdSeq() {
        cmdseq = cmdseq + 1;
        while (activeSenders.containsKey(cmdseq) || activeReceivers.containsKey(cmdseq)) {
            cmdseq = cmdseq + 1;
        }
        return cmdseq;
    }
    
	@Override
    public Collection<Command> getActiveCommands() {
        List<Command> l = new ArrayList<Command>();
        synchronized(activeSenders) {
            l.addAll(activeSenders.values());
        }
        return l;
    }
    
    @Override
    public Collection<RequestHandler> getActiveHandlers() {
        List<RequestHandler> l = new ArrayList<RequestHandler>();
        synchronized(activeReceivers) {
            l.addAll(activeReceivers.values());
        }
        return l;
    }

    @Override
    public void unregisterCommand(Command cmd) {
        Object removed;
        synchronized(activeSenders) {
            removed = activeSenders.remove(cmd.getId());
        }
        if (removed == null) {
            logger.warn("Attempted to unregister unregistered command with id " + cmd.getId());
        }
        else {
            cmd.setId(RequestReply.NOID);
        }
    }

    @Override
    public void registerHandler(RequestHandler handler, int tag) {
        synchronized(activeReceivers) {
            activeReceivers.put(tag, handler);
        }
        handler.setChannel(this);
    }

    @Override
    public void unregisterHandler(int tag) {
        Object removed;
        synchronized(activeReceivers) {
            removed = activeReceivers.remove(tag);
        }
        if (removed == null) {
            logger.warn("Attempted to unregister unregistered handler with id " + tag);
        }
    }
    
    @Override
    public Command getRegisteredCommand(int id) {
        synchronized(activeSenders) {
            return activeSenders.get(id);
        }
    }

    @Override
    public RequestHandler getRegisteredHandler(int id) {
        synchronized(activeReceivers) {
            return activeReceivers.get(id);
        }
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

	@Override
	public void sendTaggedData(int i, boolean fin, byte[] bytes) {
		sendTaggedData(i, fin, bytes, null);
	}

	@Override
	public void sendTaggedData(int tag, boolean fin, byte[] data, SendCallback cb) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + " REQ>: tag = " + tag + ", fin = " + fin + ", datalen = "
					+ data.length + ", data = " + ppByteBuf(data));
		}
		sendTaggedData(tag, fin ? FINAL_FLAG : 0, data, cb);
	}

	@Override
	public void sendTaggedData(int i, int flags, byte[] bytes) {
		sendTaggedData(i, flags, bytes, null);
	}

	@Override
	public void sendTaggedReply(int tag, byte[] data, int flags) {
		sendTaggedReply(tag, data, flags, null);
	}

	@Override
	public void sendTaggedReply(int tag, byte[] data, int flags, SendCallback cb) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + " REPL>: tag = " + tag + ", fin = " + flagIsSet(flags, FINAL_FLAG) + ", datalen = "
					+ data.length + ", data = " + ppByteBuf(data));
		}
		
		sendTaggedData(tag, flags | REPLY_FLAG, data, cb);
	}
	
	@Override
	public void sendTaggedReply(int id, ByteBuffer buf, boolean fin, boolean err, SendCallback cb) {
		sendTaggedReply(id, buf, (fin ? FINAL_FLAG : 0) + (err ? ERROR_FLAG : 0), cb);
	}

	@Override
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

	@Override
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

	@Override
	public final UserContext getUserContext() {
	    return userContext;
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

	@Override
	public RequestManager getRequestManager() {
		return requestManager;
	}

	@Override
	public void setRequestManager(RequestManager rm) {
		if (rm == null) {
			throw new IllegalArgumentException("The request manager cannot be null");
		}
		this.requestManager = rm;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void close() {
		closed = true;
		notifyListeners(null);
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public void setLocalShutdown() {
		this.localShutdown = true;
	}

	public boolean isLocalShutdown() {
		return localShutdown;
	}

	@Override
	public boolean isClient() {
		return client;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
	    if (logger.isInfoEnabled()) {
	        logger.info(this + " setting name to " + name);
	    }
		this.name = name;
	}

	public String toString() {
		return getName();
	}

	protected void handleReply(int tag, int flags, int len, byte[] data) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + " REPL<: tag = " + tag + ", fin = " + 
					flagIsSet(flags, FINAL_FLAG) + ", err = " + flagIsSet(flags, ERROR_FLAG)
					+ ", datalen = " + len + ", data = " + ppByteBuf(data));
		}
		updateLastTime();
		Command cmd = getRegisteredCommand(tag);
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
		RequestHandler handler = getRegisteredHandler(tag);
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
				    	logger.info("Registered handlers: " + getActiveHandlers());
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

	@Override
	public void flush() throws IOException {
	    // sub-classes should implement this
	}

	@Override
	public SelectableChannel getNIOChannel() {
		return null;
	}

	@Override
	public synchronized void handleChannelException(Exception e) {
	    if (closed) {
	        // handled previously
	        return;
	    }
	    for (Command cmd : getActiveCommands()) {
	        cmd.errorReceived("Channel error", e);
	    }
	    for (RequestHandler hnd : getActiveHandlers()) {
	        hnd.errorReceived("Channel error", e);
	    }
	    notifyListeners(e);
        close();
    }

	@Override
	public long getLastHeartBeat() {
        return lastHeartBeat;
    }

	@Override
    public void setLastHeartBeat(long lastHeartBeat) {
        this.lastHeartBeat = lastHeartBeat;
    }

    public Service getService() {
        return service;
    }
    
    public void setService(Service service) {
        this.service = service;
    }

    public synchronized void addListener(ChannelListener l) {
        if (listeners == null) {
            listeners = new LinkedList<ChannelListener>();
        }
        listeners.add(l);
    }
    
    public synchronized void removeListener(ChannelListener l) {
        if (listeners != null) {
            listeners.remove(l);
        }
    }

    public synchronized void notifyListeners(Exception e) {
        if (listeners != null) {
            for (ChannelListener l : listeners) {
                try {
                    l.channelClosed(this, e);
                }
                catch (Exception ee) {
                    logger.warn("Failed to notify listener of channel shutdown", ee);
                }
            }
            listeners = null;
        }
    }

    @Override
    public boolean supportsOption(Type type, Object value) {
        return false;
    }

    @Override
    public void setOption(Type type, Object value) {
    }
}
