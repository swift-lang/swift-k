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
 * Created on Aug 22, 2005
 */
package org.globus.cog.coaster.channels;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.Client;
import org.globus.cog.coaster.ClientRequestManager;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.UserContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class ChannelManager {
	private static final Logger logger = Logger.getLogger(ChannelManager.class);

	private static ChannelManager manager;
	private Map<CoasterChannel, HostCredentialPair> rchannels;
	private Map<HostCredentialPair, CoasterChannel> channels;
	private RequestManager clientRequestManager;
	
	public synchronized static ChannelManager getManager() {
		if (manager == null) {
			manager = new ChannelManager();
		}
		return manager;
	}

	private ChannelManager() {
		channels = new HashMap<HostCredentialPair, CoasterChannel>();
		rchannels = new HashMap<CoasterChannel, HostCredentialPair>();
		clientRequestManager = new ClientRequestManager();
	}

	protected void setClientRequestManager(RequestManager crm) {
		this.clientRequestManager = crm;
	}

	public CoasterChannel getExistingChannel(String host, GSSCredential cred) {
		if (host == null) {
			throw new NullPointerException("Host is null");
		}
		host = normalize(host);
		CoasterChannel channel;
		synchronized (channels) {
			HostCredentialPair hcp = new HostCredentialPair(host, cred);
			channel = getChannel(hcp);
		}
		if (channel == null) {
		    throw new IllegalStateException("No such channel: " + host);
		}
		return channel;
	}
	
	private String normalize(String url) {
		boolean https = false;
		StringBuffer sb = new StringBuffer();
		int pi = url.indexOf("://");
		if (url.startsWith("https://")) {
			https = true;
		}
		if (pi == -1) {
			sb.append("https://");
			https = true;
			pi = -3;
		}
		else {
			sb.append(url.substring(0, pi + 3));
		}
		sb.append(url.substring(pi + 3));
		int hi = url.indexOf(':', pi + 3);
		if (hi == -1 && https) {
			sb.append(":1984");
		}
		return sb.toString();
	}
	
	public void removeChannel(CoasterChannel channel) {
	    synchronized (channels) {
	        HostCredentialPair hcp = rchannels.remove(channel);
	        if (hcp != null) {
	            channels.remove(hcp);
	        }
	    }
    }

	public void registerChannel(String url, GSSCredential cred, CoasterChannel channel) {
		synchronized (channels) {
			HostCredentialPair hcp = new HostCredentialPair(url, cred);
			CoasterChannel previous = getChannel(hcp);
			if (previous != null) {
			    throw new IllegalStateException("A channel already exists for this key: " + hcp);
			}
			channels.put(hcp, channel);
			rchannels.put(channel, hcp);
		}
	}
	
	public void registerChannel(String url, CoasterChannel channel) {
	    registerChannel(url, null, channel);
    }

	private CoasterChannel getChannel(HostCredentialPair key) {
		CoasterChannel channel =  channels.get(key);
		if (logger.isDebugEnabled()) {
			logger.debug("getChannel(" + key + "): " + channel);
		}
		return channel;
	}

	private boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	public CoasterChannel getOrCreateChannel(String host, GSSCredential cred, RequestManager rm)
			throws ChannelException {
		CoasterChannel channel = getExistingChannel(host, cred);
		if (channel == null) {
		    channel = openChannel(host, cred, rm);
		}
		return channel;
	}

	public CoasterChannel openChannel(String host, GSSCredential cred, RequestManager rm) throws ChannelException {
		try {
			Client client = Client.getClient(host, new UserContext(cred), rm);
			CoasterChannel channel = client.getChannel();
			registerChannel(host, cred, channel);
			return channel;
		}
		catch (ChannelException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ChannelException(e);
		}
	}
	
	private static class HostCredentialPair {
		private String host, DN;

		public HostCredentialPair(String host, GSSCredential cred) {
			this(host, (String) null);
			try {
				if (cred != null) {
					DN = String.valueOf(cred.getName());
				}
				else {
					DN = null;
				}
			}
			catch (GSSException e) {
			}
		}

		public HostCredentialPair(String host, String DN) {
			this.host = host;
			this.DN = DN;
		}

		public boolean equals(Object obj) {
			if (obj instanceof HostCredentialPair) {
				HostCredentialPair other = (HostCredentialPair) obj;
				return host.equals(other.host);
			}
			return false;
		}

		public int hashCode() {
			return host.hashCode();
		}

		public String toString() {
			return DN + "@" + host;
		}

	}
}
