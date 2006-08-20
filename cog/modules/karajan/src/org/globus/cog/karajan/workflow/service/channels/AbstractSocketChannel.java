//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2006
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.io.EOFException;
import java.net.Socket;

import org.globus.cog.karajan.workflow.service.RequestManager;

public abstract class AbstractSocketChannel extends AbstractStreamKarajanChannel implements Runnable {
	private Socket socket;
	private boolean started;
	private Exception startException;
	private final boolean client;
	private boolean closing;

	public AbstractSocketChannel(RequestManager requestManager, ChannelContext channelContext,
			Socket socket, boolean client) {
		super(requestManager, channelContext);
		this.socket = socket;
		this.client = client;
		if (client) {
			setEndpoint("C(" + socket.getLocalAddress() + ")");
		}
		else {
			setEndpoint("S(" + socket.getLocalAddress() + ")");
		}
	}
	
	public synchronized void start() throws Exception {
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.setName("Chanel: " + getEndpoint());
		thread.start();
		while (!isStarted() && !isClosed() && startException == null) {
			try {
				wait();
			}
			catch (InterruptedException e) {
			}
		}
		if (startException != null) {
			logger.debug("Exception while starting channel", startException);
			throw startException;
		}
		logger.info(getEndpoint() + "Channel started");
	}

	public void run() {
		ChannelContext context = getChannelContext();
		try {
			try {
				setInputStream(socket.getInputStream());
				setOutputStream(socket.getOutputStream());
				started = true;
			}
			catch (Exception e) {
				startException = e;
				e.printStackTrace();
				return;
			}
			finally {
				synchronized (this) {
					notifyAll();
				}
			}
			initializeConnection();
			mainLoop();
		}
		catch (EOFException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Channel terminated", e);
			}
			context.notifyRegisteredListeners(e);
		}
		catch (Exception e) {
			if (!closing) {
				logger.warn("Exception in channel", e);
				context.notifyRegisteredListeners(e);
			}
		}
		finally {
			try {
				setLocalShutdown();
				ChannelManager.getManager().shutdownChannel(this);
			}
			catch (ShuttingDownException e) {
				logger.debug("Channel already shutting down");
			}
			catch (Exception e) {
				logger.warn(getEndpoint() + "Could not shutdown channel", e);
			}
			super.close();
			synchronized (this) {
				notify();
			}
			logger.info(getEndpoint() + "Channel terminated");
		}
	}

	protected void initializeConnection() {

	}

	public void close() {
		closing = true;
		try {
			if (!socket.isClosed()) {
				socket.close();
				logger.info(getEndpoint() + "Channel shut down");
			}
		}
		catch (Exception e) {
			logger.warn(getEndpoint() + "Failed to close socket", e);
		}
		super.close();
	}

	public boolean isClient() {
		return client;
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isOffline() {
		return isClosed();
	}
}
