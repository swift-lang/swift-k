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
 * Created on Jul 19, 2005
 */
package org.globus.cog.coaster.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.RequestReply;
import org.globus.cog.coaster.channels.ChannelIOException;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.SendCallback;

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

	public void waitForReply() throws InterruptedException {
		synchronized (this) {
			while (!this.isInDataReceived()) {
				wait(0);
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

	    if (this.isInDataReceived()) {
	        // probably channel died in-between registration and send, so don't bother
	        return;
	    }
		CoasterChannel channel = getChannel();
		if (logger.isDebugEnabled()) {
			logger.debug("Sending " + this + " on " + channel);
		}
		Collection<byte[]> outData = getOutData();
		if (channel == null) {
			throw new ProtocolException("Unregistered command");
		}
		
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
			int flags;
			boolean fin = (outData == null) || (outData.size() == 0);
			if (fin) {
				flags = CoasterChannel.FINAL_FLAG + CoasterChannel.INITIAL_FLAG;;
			}
			else {
				flags = CoasterChannel.INITIAL_FLAG;;
			}

			channel.sendTaggedData(id, flags, getOutCmd().getBytes(), fin ? this : null);
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
	}
	
	private static boolean shutdownMsg;

	public byte[] execute(CoasterChannel channel) throws ProtocolException, IOException, InterruptedException {
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

	public void executeAsync(CoasterChannel channel, Callback cb) throws ProtocolException {
		this.cb = cb;
		send(channel);
	}

	public void executeAsync(CoasterChannel channel) throws ProtocolException {
		send(channel);
	}

	protected void send(CoasterChannel channel) throws ProtocolException {
		channel.registerCommand(this);
		send();
	}
	
	protected void unregister() {
        this.getChannel().unregisterCommand(this);
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
				CoasterChannel channel = ChannelManager.getManager().reserveChannel(
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

	public String toString() {
		return "Command(tag: " + this.getId() + ", " + this.getOutCmd() + ")";
	}

	public static interface Callback {
		void replyReceived(Command cmd);

		void errorReceived(Command cmd, String msg, Exception t);
	}
}
