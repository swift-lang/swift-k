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
 * Created on Feb 14, 2008
 */
package org.globus.cog.coaster;

import java.net.URI;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.coaster.channels.ChannelException;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.GSSChannel;
import org.globus.cog.coaster.channels.TCPChannel;
import org.globus.cog.coaster.channels.UDPChannel;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

public class ChannelFactory {
	public static final Logger logger = Logger.getLogger(ChannelFactory.class);

	public static final int DEFAULT_CREDENTIAL_REFRESH_INTERVAL = 30000;
	private static GSSCredential cachedCredential;
	private static long credentialTime;

	public static CoasterChannel newChannel(URI contact, UserContext context, RequestManager rm)
			throws ChannelException {
		CoasterChannel channel;
		if (context == null) {
		    context = new UserContext();
		}
		try {
			if (contact.getScheme() == null || contact.getScheme().equals("tcps")) {
				ensureCredentialPresent(context);
				channel = new GSSChannel(contact, rm, context);
			}
			else if (contact.getScheme().equals("https")) {
				ensureCredentialPresent(context);
				channel = new GSSChannel(contact, rm, context);
			}
			else if (contact.getScheme().equals("tcp") || contact.getScheme().equals("http")) {
				channel = new TCPChannel(contact, context, rm);
			}
			else if (contact.getScheme().equals("udp")) {
				channel = new UDPChannel(contact, context, rm);
			}
			else {
				throw new IllegalArgumentException("Scheme not supported: " + contact);
			}
			channel.start();
			return channel;
		}
		catch (InvalidSecurityContextException e) {
			throw new ChannelException(e);
		}
	}

	private static void ensureCredentialPresent(UserContext context)
			throws InvalidSecurityContextException {
		if (context.getCredential() == null) {
			context.setCredential(getDefaultCredential());
		}
	}

	public static GSSCredential getDefaultCredential() throws InvalidSecurityContextException {
		synchronized (ChannelFactory.class) {
			if (cachedCredential == null
					||
					(System.currentTimeMillis() - credentialTime) > DEFAULT_CREDENTIAL_REFRESH_INTERVAL) {
				credentialTime = System.currentTimeMillis();
				GSSManager manager = ExtendedGSSManager.getInstance();
				try {
					cachedCredential = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
				}
				catch (GSSException e) {
					throw new InvalidSecurityContextException(e);
				}
			}
			return cachedCredential;
		}
	}
}
