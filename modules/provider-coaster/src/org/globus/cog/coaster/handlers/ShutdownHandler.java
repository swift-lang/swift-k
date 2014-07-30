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

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.ChannelException;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.ShuttingDownException;

public class ShutdownHandler extends RequestHandler {
	private static final Logger logger = Logger.getLogger(ShutdownHandler.class);
	
	public void requestComplete() throws ProtocolException {
		try {
			getChannel().setLocalShutdown();
			ChannelManager.getManager().shutdownChannel(getChannel());
			sendReply("OK".getBytes());
			//getChannel().close();
		}
		catch (ShuttingDownException e) {
			logger.info("Channel is already shutting down", e);
		}
		catch (ChannelException e) {
			logger.warn("Cannot shut down channel", e);
			sendError("Cannot shut down channel");
		}
	}
}
