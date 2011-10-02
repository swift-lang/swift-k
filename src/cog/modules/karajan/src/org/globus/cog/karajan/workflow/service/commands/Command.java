//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 19, 2005
 */
package org.globus.cog.karajan.workflow.service.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RequestReply;
import org.globus.cog.karajan.workflow.service.TimeoutException;
import org.globus.cog.karajan.workflow.service.channels.ChannelIOException;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.channels.SendCallback;

public abstract class Command extends RequestReply implements SendCallback {
	private static final Logger logger = Logger.getLogger(Command.class);

	private static final Timer timer;

	static {
		timer = new Timer(true);
	}

	public static final int DEFAULT_MAX_RETRIES = 2;
	private int maxRetries = DEFAULT_MAX_RETRIES;

	private Callback cb;
	private String errorMsg;
	private Exception exception;
	private int retries;
	private long sendTime;
	private long sendReqTime;

	public Command() {
		setId(NOID);
	}

	public Command(String cmd) {
		this();
		this.setOutCmd(cmd);
	}

	public void setCallback(Callback cb) {
		this.cb = cb;
	}

	public void waitForReply() throws TimeoutException {
		synchronized (this) {
			if (!this.isInDataReceived()) {
				long left = getTimeout();
				while (!this.isInDataReceived()) {
					if (left <= 0) {
						throw new TimeoutException();
					}
					try {
						wait(left);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					left = sendTime == 0 ? 1000 : getTimeout()
							- (System.currentTimeMillis() - sendTime);
				}
			}
		}
	}

	public void dataReceived(boolean fin, boolean err, byte[] data) throws ProtocolException {
		super.dataReceived(fin, err, data);
		this.addInData(fin, err, data);
	}

	public void replyReceived(boolean fin, boolean err, byte[] data) throws ProtocolException {
		this.dataReceived(fin, err, data);
	}
	
	/** 
	   @deprecated
	 */
	protected final void replyReceived(byte[] data) {
		throw new RuntimeException("Do not use this");
	}
	
	protected final void dataReceived(byte[] data) {
		throw new RuntimeException("Do not use this");
	}
	
	public void send(boolean err) throws ProtocolException {
		sendReqTime = System.currentTimeMillis();
		setLastTime(sendReqTime);

		KarajanChannel channel = getChannel();
		if (logger.isDebugEnabled()) {
			logger.debug("Sending " + this + " on " + channel);
		}
		Collection<byte[]> outData = getOutData();
		if (channel == null) {
			throw new ProtocolException("Unregistered command");
		}
		boolean fin = (outData == null) || (outData.size() == 0);

		if (logger.isDebugEnabled()) {
			logger.debug(ppOutData("CMD"));
		}
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(this + " CMD: " + this);
			}
			int id = getId();
			if (id == NOID) {
				logger.warn("Command has NOID: " + this, new Throwable());
			}
			channel.sendTaggedData(id, fin, getOutCmd().getBytes(), fin ? this : null);
			if (!fin) {
				Iterator<byte[]> i = outData.iterator();
				while (i.hasNext()) {
					byte[] buf = i.next();
					channel.sendTaggedData(id, !i.hasNext(), buf, !i.hasNext() ? this : null);
				}
			}
		}
		catch (ChannelIOException e) {
			reexecute(e.getMessage(), e);
		}
	}

	public void dataSent() {
		sendTime = System.currentTimeMillis();
		//when using the piped channels the reply will arrive before this method is called
		setLastTime(sendTime);
	}
	
	private static boolean shutdownMsg;

	public byte[] execute(KarajanChannel channel) throws ProtocolException, IOException {
		send(channel);
		waitForReply();
		if (errorMsg != null) {
			throw new ProtocolException(errorMsg, exception);
		}
		if (exception != null) {
			throw new ProtocolException(exception);
		}
		return getInData();
	}

	public void executeAsync(KarajanChannel channel, Callback cb) throws ProtocolException {
		this.cb = cb;
		send(channel);
	}

	public void executeAsync(KarajanChannel channel) throws ProtocolException {
		send(channel);
	}

	protected void send(KarajanChannel channel) throws ProtocolException {
		channel.registerCommand(this);
		send();
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void receiveCompleted() {
		if (logger.isDebugEnabled()) {
			logger.debug(ppInData("CMD"));
		}
		super.receiveCompleted();
		if (cb != null) {
			cb.replyReceived(this);
		}
	}

	public void errorReceived(String msg, Exception t) {
		if (logger.isDebugEnabled()) {
			logger.debug(ppInData("CMDERR"));
		}
		if (cb != null) {
			cb.errorReceived(this, msg, t);
		}
		else {
			this.errorMsg = msg;
			this.exception = t;
		}
		super.receiveCompleted();
	}

	protected String ppOutData(String prefix) {
		return ppData(prefix + "> ", getOutCmd(), getOutData());
	}

	protected String ppInData(String prefix) {
		return ppData(prefix + "< ", getOutCmd(), getInDataChunks());
	}

	public void channelClosed() {
		if (!this.isInDataReceived()) {
			reexecute("Channel closed", null);
		}
	}

	protected void reexecute(String message, Exception ex) {
		if (++retries > maxRetries) {
			logger.info(this + ": failed too many times", ex);
			errorReceived(message, ex);
		}
		else {
			logger.info(this + ": re-sending");
			logger.warn(this + "fault was: " + message, ex);
			try {
				KarajanChannel channel = ChannelManager.getManager().reserveChannel(
						getChannel().getChannelContext());
				setChannel(channel);
				if (getId() == NOID) {
					channel.registerCommand(this);
				}
				send();
			}
			catch (ProtocolException e) {
				errorReceived(e.getMessage(), e);
			}
			catch (Exception e) {
				reexecute(e.getMessage(), ex);
			}
		}
	}
	
	public void handleTimeout() {
		if (isInDataReceived()) {
			return;
		}
		logger.warn(this
				+ ": handling reply timeout; sendReqTime="
				+ DF.format(new Date(sendReqTime)) + ", sendTime=" + DF.format(new Date(sendTime))
						+ ", now=" + DF.format(new Date()));
		reexecute("Reply timeout", new TimeoutException());
	}

	protected long getSendReqTime() {
		return sendReqTime;
	}

	protected void setSendReqTime(long sendReqTime) {
		this.sendReqTime = sendReqTime;
	}

	public String toString() {
		return "Command(" + this.getId() + ", " + this.getOutCmd() + ")";
	}

	public static interface Callback {
		void replyReceived(Command cmd);

		void errorReceived(Command cmd, String msg, Exception t);
	}
}
