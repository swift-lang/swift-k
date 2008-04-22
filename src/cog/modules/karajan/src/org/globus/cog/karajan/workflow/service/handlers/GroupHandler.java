//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.karajan.workflow.service.handlers;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.NoSuchHandlerException;
import org.globus.cog.karajan.workflow.service.ProtocolException;

public abstract class GroupHandler extends RequestHandler {
	private static final Logger logger = Logger.getLogger(GroupHandler.class);
	
	private int[] membertags;
	private int index;
	private RequestHandler crtHandler;
	

	public void requestComplete() throws ProtocolException {
		if (crtHandler == null) {
			byte[] btags = getInData(0);
			ByteBuffer tags = ByteBuffer.wrap(btags);
			int ntags = btags.length / 4;
			membertags = new int[ntags];
			for (int i = 0; i < ntags; i++) {
				int tag = tags.getInt();
				/*
				 * This relies on the fact that the channel will not process the
				 * next request until this method completes. Otherwise, a race
				 * condition will occur.
				 */
				if (logger.isDebugEnabled()) {
					logger.debug("Tag: "+tag);
				}
				this.getChannel().registerHandler(this, tag);
				membertags[i] = tag;
			}
			index = 0;
		}
		else {
			handlerRequestComplete(crtHandler);
			crtHandler = null;
		}
	}

	protected void handlerRequestComplete(RequestHandler handler) throws ProtocolException {
		handler.requestComplete();
	}

	public void dataReceived(byte[] data) throws ProtocolException {
		if (membertags == null) {
			super.dataReceived(data);
		}
		else {
			// sub-commands
			if (crtHandler == null) {
				try {
					crtHandler = this.getChannel().getRequestManager().handleInitialRequest(data);
					crtHandler.register(getChannel());
					crtHandler.setId(membertags[index++]);
				}
				catch (NoSuchHandlerException e) {
					throw new ProtocolException(e);
				}
			}
			else {
				handlerDataReceived(crtHandler, data);
			}
		}
	}

	protected void handlerDataReceived(RequestHandler handler, byte[] data)
			throws ProtocolException {
		handler.dataReceived(data);
	}

	public void errorReceived(String msg, Exception t) {
		if (crtHandler != null) {
			handlerErrorReceived(crtHandler, msg, t);
			crtHandler = null;
		}
	}
	
	protected void handlerErrorReceived(RequestHandler handler, String msg, Exception e) {
		handler.errorReceived(msg, e);
	}

	public RequestHandler getCrtHandler() {
		return crtHandler;
	}
}
