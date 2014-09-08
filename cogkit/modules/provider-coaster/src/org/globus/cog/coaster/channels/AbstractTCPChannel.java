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
 * Created on Jul 21, 2006
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.UserContext;

public abstract class AbstractTCPChannel extends AbstractStreamCoasterChannel {
    public static final Logger logger = Logger.getLogger(AbstractTCPChannel.class);
    
	private Socket socket;
	private boolean started;
	private Exception startException;
	private boolean closing;
	private SocketChannel nioChannel;
	
	public static final boolean logPerformanceData;
	
	static {
		logPerformanceData = "true".equals(System.getProperty("tcp.channel.log.io.performance"));
	}

	public AbstractTCPChannel(RequestManager requestManager, UserContext userContext, boolean client) {
		super(requestManager, userContext, client);
	}
	
	protected void setSocket(Socket socket) throws IOException {
		this.socket = socket;
		if (logPerformanceData) {
			setInputStream(new PerformanceDiagnosticInputStream(socket.getInputStream()));
			setOutputStream(new PerformanceDiagnosticOutputStream(socket.getOutputStream()));
		}
		else {
			setInputStream(socket.getInputStream());
			setOutputStream(socket.getOutputStream());
		}
		
		this.nioChannel = socket.getChannel();
		if (this.nioChannel != null) {
			this.nioChannel.configureBlocking(false);
		}
	}
	
	protected Socket getSocket() {
		return socket;
	}

	public synchronized void start() throws ChannelException {
		initialize();
		if (logger.isInfoEnabled()) {
			logger.info(this + ": channel started");
		}
		super.start();
	}

	private void initialize() throws ChannelException {
		try {
			initializeConnection();
			register();
			started = true;
		}
		catch (Exception e) {
			logger.debug("Exception while starting channel", e);
			throw new ChannelException(e);
		}
	}

	protected void initializeConnection() {

	}

	public void shutdown() {
	    unregister();
		if (isLocalShutdown()) {
			return;
		}
		setLocalShutdown();
		ChannelManager.getManager().removeChannel(this);
		super.close();
		synchronized (this) {
			notify();
		}
		logger.info(this + ": channel terminated");
	}

	public void close() {
		synchronized(this) {
			if (closing || socket == null) {
				return;
			}
			closing = true;
		}
		try {
			if (!socket.isClosed()) {
				socket.close();
				logger.info(this + ": channel shut down");
			}
		}
		catch (Exception e) {
			logger.warn(this + ": Failed to close socket", e);
		}
		super.close();
	}

	public boolean isStarted() {
		return started;
	}

	@Override
	public SelectableChannel getNIOChannel() {
		return nioChannel;
	}
	
	public String toString() {
        if (getName() == null) {
            return getClass().getSimpleName() + "[" + (isClient() ? "client" : "server") + ", " + getPeerName() + "]";
        }
        else {
            return getName();
        }
    }
	
	private String getPeerName() {
        if (getContact() != null) {
            return getContact().toString();
        }
        Socket sock = getSocket();
        if (sock != null) {
            return sock.getInetAddress().getHostAddress() + ":" + sock.getPort();
        }
        return "unknown";
    }
}
