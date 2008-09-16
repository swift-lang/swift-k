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
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.ReplyTimeoutException;
import org.globus.cog.karajan.workflow.service.RequestReply;
import org.globus.cog.karajan.workflow.service.channels.ChannelIOException;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public abstract class Command extends RequestReply {
	private static final Logger logger = Logger.getLogger(Command.class);

	private static final Timer timer;

	static {
		timer = new Timer();
	}

	public static final int DEFAULT_REPLY_TIMEOUT = 10000 * 60;
	public static final int DEFAULT_MAX_RETRIES = 2;
	private int replyTimeout = DEFAULT_REPLY_TIMEOUT;
	private int maxRetries = DEFAULT_MAX_RETRIES;

	private Callback cb;
	private String errorMsg;
	private Exception exception;
	private Timeout timeout;
	private int retries;

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

	public void waitForReply() throws ReplyTimeoutException {
		if (!this.isInDataReceived()) {
			synchronized (this) {
				long start = System.currentTimeMillis();
				long left = replyTimeout;
				while (!this.isInDataReceived()) {
					try {
						wait(left);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					left = replyTimeout - (System.currentTimeMillis() - start);
					if (left <= 0) {
						throw new ReplyTimeoutException();
					}
				}
			}
		}
	}

	public void dataReceived(byte[] data) throws ProtocolException {
		super.dataReceived(data);
		this.addInData(data);
	}

	public void replyReceived(byte[] data) throws ProtocolException {
		this.dataReceived(data);
	}

	public void send() throws ProtocolException {
		KarajanChannel channel = getChannel();
		logger.info("Sending " + this + " on " + channel);
		List outData = getOutData();
		if (channel == null) {
			throw new ProtocolException("Unregistered command");
		}
		boolean fin = (outData == null) || (outData.size() == 0);

		if (logger.isDebugEnabled()) {
			logger.debug(ppOutData("CMD"));
		}
		try {
			if (logger.isInfoEnabled()) {
				logger.info(this + " CMD: " + this);
			}
			channel.sendTaggedData(getId(), fin, getOutCmd().getBytes());
			if (!fin) {
				Iterator i = outData.iterator();
				while (i.hasNext()) {
					byte[] buf = (byte[]) i.next();
					channel.sendTaggedData(getId(), !i.hasNext(), buf);
				}
			}
			timer.schedule(timeout = new Timeout(), replyTimeout);
		}
		catch (ChannelIOException e) {
			reexecute(e.getMessage(), e);
		}
	}

	public byte[] execute(KarajanChannel channel) throws ProtocolException, IOException {
		executeAsync(channel);
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
		channel.registerCommand(this);
		send();
	}

	public void executeAsync(KarajanChannel channel) throws ProtocolException {
		channel.registerCommand(this);
		send();
	}

	public int getReplyTimeout() {
		return replyTimeout;
	}

	public void setReplyTimeout(int replyTimeout) {
		this.replyTimeout = replyTimeout;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void receiveCompleted() {
		if (timeout == null) {
			return;
		}
		else {
			timeout.cancel();
			timeout = null;
		}
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
		this.errorMsg = msg;
		this.exception = t;
		if (cb != null) {
			cb.errorReceived(this, msg, t);
		}
		super.receiveCompleted();
	}

	protected String ppOutData(String prefix) {
		return ppData(prefix + "> ", getOutCmd(), getOutData());
	}

	protected String ppInData(String prefix) {
		return ppData(prefix + "< ", getOutCmd(), getInDataChuncks());
	}

	public void channelClosed() {
		if (!this.isInDataReceived()) {
			reexecute("Channel closed", null);
		}
	}

	protected void reexecute(String message, Exception ex) {
		if (++retries > maxRetries) {
			errorReceived(message, ex);
		}
		else {
			try {
				setChannel(ChannelManager.getManager().reserveChannel(
						getChannel().getChannelContext()));
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

	protected void handleReplyTimeout() {
		timeout = null;
		reexecute("Reply timeout", new ReplyTimeoutException());
	}

	private class Timeout extends TimerTask {
		public void run() {
			handleReplyTimeout();
		}
	}

	public String toString() {
		return "Command(" + this.getOutCmd() + ")";
	}

	public static interface Callback {
		void replyReceived(Command cmd);

		void errorReceived(Command cmd, String msg, Exception t);
	}
}
