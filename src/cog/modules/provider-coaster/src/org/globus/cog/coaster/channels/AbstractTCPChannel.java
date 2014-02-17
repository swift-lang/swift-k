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

	public AbstractTCPChannel(RequestManager requestManager, ChannelContext channelContext,
			boolean client) {
		super(requestManager, channelContext, client);
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
		if (isClient()) {
			setName("C(" + getContact() + ")");
		}
		else {
			setName("S(" + socket.getLocalAddress() + ")");
		}
		initialize();
		if (logger.isInfoEnabled()) {
			logger.info("Channel started: " + this);
		}
		if (isClient()) {
			try {
				configure();
			}
			catch (Exception e) {
				throw new ChannelException("Failed to configure channel", e);
			}
		}
	}

	private void initialize() throws ChannelException {
		ChannelContext context = getChannelContext();
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
		try {
			setLocalShutdown();
			ChannelManager.getManager().shutdownChannel(this);
		}
		catch (ShuttingDownException e) {
			logger.debug("Channel already shutting down");
		}
		catch (Exception e) {
			logger.warn(getContact() + ": Could not shutdown channel", e);
		}
		super.close();
		synchronized (this) {
			notify();
		}
		logger.info(getContact() + ": Channel terminated");
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
				logger.info(getContact() + ": Channel shut down");
			}
		}
		catch (Exception e) {
			logger.warn(getContact() + ": Failed to close socket", e);
		}
		super.close();
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isOffline() {
		return isClosed();
	}

	@Override
	public SelectableChannel getNIOChannel() {
		return nioChannel;
	}
}
