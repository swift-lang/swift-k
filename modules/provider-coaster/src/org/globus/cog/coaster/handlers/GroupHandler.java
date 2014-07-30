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
 * Created on Jul 21, 2005
 */
package org.globus.cog.coaster.handlers;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.NoSuchHandlerException;
import org.globus.cog.coaster.ProtocolException;

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

	public void dataReceived(boolean fin, boolean err, byte[] data) throws ProtocolException {
		if (membertags == null) {
			super.dataReceived(fin, err, data);
		}
		else {
			// sub-commands
			if (crtHandler == null) {
				try {
					crtHandler = this.getChannel().getRequestManager().handleInitialRequest(getId(), data);
					crtHandler.setChannel(getChannel());
					crtHandler.setId(membertags[index++]);
				}
				catch (NoSuchHandlerException e) {
					throw new ProtocolException(e);
				}
			}
			else {
				handlerDataReceived(crtHandler, fin, err, data);
			}
		}
	}

	protected void handlerDataReceived(RequestHandler handler, boolean fin, boolean err, byte[] data)
			throws ProtocolException {
		handler.dataReceived(fin, err, data);
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
