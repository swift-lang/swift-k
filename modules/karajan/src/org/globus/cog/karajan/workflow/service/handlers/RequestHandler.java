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
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RequestReply;
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
		send();
		replySent = true;
	}
	
	public boolean isReplySent() {
		return replySent;
	}
	
	protected void unregister() {
		this.getChannel().unregisterHandler(this.getId());
	}
	
	public void dataReceived(byte[] data) throws ProtocolException {
		super.dataReceived(data);
		if (getInCmd() == null) {
			setInCmd(new String(data));
		}
		else {
			this.addInData(data);
		}
	}
	
	public void send() throws ProtocolException {
		KarajanChannel channel = getChannel();
		Collection outData = getOutData();
		if (channel == null) {
			throw new ProtocolException("Unregistered command");
		}
		if (logger.isDebugEnabled()) {
			logger.debug(ppOutData("HND"));
		}
		boolean fin = (outData == null) || (outData.size() == 0);
		if (!fin) {
			Iterator i = outData.iterator();
			while (i.hasNext()) {
				byte[] buf = (byte[]) i.next();
				channel.sendTaggedReply(getId(), buf, !i.hasNext(), getErrorFlag());
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
		return ppData(prefix+"< ", getInCmd(), getInDataChuncks());
	}
	
	public String toString() {
		return "Handler(" + getInCmd() + ")";
	}
}
