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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.RemoteConfiguration;
import org.globus.cog.coaster.channels.ChannelContext;
import org.globus.cog.coaster.channels.ChannelException;
import org.globus.cog.coaster.channels.ChannelID;
import org.globus.cog.coaster.channels.ChannelManager;

public class ChannelConfigurationHandler extends RequestHandler {
	private static final Logger logger = Logger.getLogger(ChannelConfigurationHandler.class);

	public void requestComplete() throws ProtocolException {
		RemoteConfiguration.Entry conf = new RemoteConfiguration.Entry(null, new String(
				getInData(0)));
		String callbackURL = new String(getInData(1));
		if (callbackURL.length() == 0) {
			callbackURL = null;
		}
		String localID = new String(getInData(2));
		String remoteID = new String(getInData(3));
		ChannelContext cc = getChannel().getChannelContext();
		ChannelID cid = cc.getChannelID();
		if (remoteID.length() == 0) {
			remoteID = ChannelID.newUID();
		}
		cid.setLocalID(localID);
		cid.setRemoteID(remoteID);
		logger.info("Channel id: " + cid.getUniqueID());
		RemoteConfiguration.Entry sconf = new RemoteConfiguration.Entry(null, translate(conf), null);
		cc.setConfiguration(sconf);
		cc.setRemoteContact(callbackURL);
		try {
			ChannelManager.getManager().registerChannel(cid, getChannel());
			sendReply(remoteID.getBytes());
		}
		catch (ChannelException e) {
			logger.debug("Could not register channel", e);
			throw new ProtocolException("Could not register channel", e);
		}
	}

	private Map<String, String> translate(RemoteConfiguration.Entry conf) {
		Map<String, String> newopts = new HashMap<String, String>();
		Iterator<String> i = conf.getOptions().iterator();
		while (i.hasNext()) {
			String opt = i.next();
			if (opt.equals(RemoteConfiguration.POLL) || opt.equals(RemoteConfiguration.RECONNECT)) {
				newopts.put(RemoteConfiguration.BUFFER, null);
			}
			else if (opt.equals(RemoteConfiguration.CALLBACK)) {
				newopts.put(RemoteConfiguration.KEEPALIVE, "120");
				newopts.put(RemoteConfiguration.RECONNECT, null);
			}
			else if (opt.equals(RemoteConfiguration.KEEPALIVE)) {
				newopts.put(RemoteConfiguration.KEEPALIVE, null);
			}
		}
		return newopts;
	}
}
