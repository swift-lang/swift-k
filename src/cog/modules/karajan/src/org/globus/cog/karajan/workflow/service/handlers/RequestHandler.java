//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.karajan.workflow.service.handlers;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RequestReply;
import org.globus.cog.karajan.workflow.service.TimeoutException;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public abstract class RequestHandler extends RequestReply {
	private static final Logger logger = Logger.getLogger(RequestHandler.class);
	
	private boolean replySent;
	
	public abstract void requestComplete() throws ProtocolException;
	
	protected void sendReply(byte[] data) throws ProtocolException {
		addOutData(data);
		sendReply();
	}
	
	protected void sendReply(String reply) throws ProtocolException {
	    sendReply(reply.getBytes());
	}
	
	protected void sendReply() throws ProtocolException {
	    setLastTime(System.currentTimeMillis());
		send();
		replySent = true;
	}
	
	public boolean isReplySent() {
		return replySent;
	}
	
	protected void unregister() {
		this.getChannel().unregisterHandler(this.getId());
	}
	
	public void dataReceived(boolean fin, boolean error, byte[] data) throws ProtocolException {
		super.dataReceived(fin, error, data);
		if (getInCmd() == null) {
			setInCmd(new String(data));
		}
		else {
			this.addInData(fin, error, data);
		}
	}
	
	public void send(boolean err) throws ProtocolException {
		KarajanChannel channel = getChannel();
		Collection<byte[]> outData = getOutData();
		if (channel == null) {
			throw new ProtocolException("Unregistered command");
		}
		if (logger.isDebugEnabled()) {
			logger.debug(ppOutData("HND"));
		}
		boolean fin = (outData == null) || (outData.size() == 0);
		if (!fin) {
			Iterator<byte[]> i = outData.iterator();
			while (i.hasNext()) {
				byte[] buf = i.next();
				channel.sendTaggedReply(getId(), buf, !i.hasNext(), err);
			}
		}
	}

	public final void receiveCompleted() {
		if (logger.isDebugEnabled()) {
			logger.debug(ppInData("HND"));
		}
		super.receiveCompleted();
		try {
			requestComplete();
		}
		catch (ProtocolException e) {
			try {
				sendError(e.getMessage(), e);
			}
			catch (ProtocolException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void errorReceived(String msg, Exception t) {
		logger.warn(msg, t);
	}
	
	protected String ppOutData(String prefix) {
		return ppData(prefix+"> ", getInCmd(), getOutData());
	}
	
	protected String ppInData(String prefix) {
		return ppData(prefix + "< ", getInCmd(), getInDataChunks());
	}

	public String toString() {
		return "Handler(" + getId() + ", " + getInCmd() + ")";
	}

	public void handleTimeout() {
		if (isInDataReceived()) {
			return;
		}
		String msg = this + ": timed out receiving request. Last time "
				+ DF.format(new Date(getLastTime())) + ", now: " + DF.format(new Date());
		logger.info(msg);
		errorReceived("Timeout", new TimeoutException(msg));
	}
}
