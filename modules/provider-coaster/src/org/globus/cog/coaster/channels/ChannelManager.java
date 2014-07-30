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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.Client;
import org.globus.cog.coaster.ClientRequestManager;
import org.globus.cog.coaster.RemoteConfiguration;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.Service;
import org.globus.cog.coaster.UserContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class ChannelManager {
	private static final Logger logger = Logger.getLogger(ChannelManager.class);

	private static ChannelManager manager;
	private Map<MetaChannel, List<HostCredentialPair>> rchannels;
	private Map<HostCredentialPair, MetaChannel> channels;
	private RemoteConfiguration config;
	private Service localService;
	private RequestManager clientRequestManager;
	
	public synchronized static ChannelManager getManager() {
		if (manager == null) {
			manager = new ChannelManager();
		}
		return manager;
	}

	private ChannelManager() {
		channels = new HashMap<HostCredentialPair, MetaChannel>();
		rchannels = new HashMap<MetaChannel, List<HostCredentialPair>>();
		config = RemoteConfiguration.getDefault();
		clientRequestManager = new ClientRequestManager();
	}

	protected void setClientRequestManager(RequestManager crm) {
		this.clientRequestManager = crm;
	}

	public CoasterChannel getExistingChannel(String host, GSSCredential cred) {
		MetaChannel channel;
		if (host == null) {
			throw new NullPointerException("Host is null");
		}
		host = normalize(host);
		synchronized (channels) {
			HostCredentialPair hcp = new HostCredentialPair(host, cred);
			channel = getChannel(hcp);
		}
		return channel;
	}
	
	private MetaChannel getClientChannel(String host, GSSCredential cred, RequestManager rm)
			throws ChannelException {
		try {
			MetaChannel channel;
			if (host == null) {
				throw new NullPointerException("Host is null");
			}
			host = normalize(host);
			synchronized (channels) {
				HostCredentialPair hcp = new HostCredentialPair(host, cred);
				channel = getChannel(hcp);
				if (channel == null) {
					ChannelContext context = new ChannelContext(host);
					context.setConfiguration(RemoteConfiguration.getDefault().find(host));
					context.newUserContext(cred);
					context.setRemoteContact(host);
					channel = new MetaChannel(rm == null ? clientRequestManager : rm, context);
					registerChannel(hcp, channel);
				}
			}
			return channel;
		}
		catch (Exception e) {
			throw new ChannelException(e);
		}
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

	public void registerChannel(String url, GSSCredential cred, CoasterChannel channel)
			throws ChannelException {
		synchronized (channels) {
			HostCredentialPair hcp = new HostCredentialPair(url, cred);
			MetaChannel previous;
			try {
				previous = getMetaChannel(channel);
			}
			catch (ChannelException e) {
				previous = new MetaChannel(channel.getRequestManager(), channel.getChannelContext());
			}
			putChannel(hcp, previous);
			previous.bind(channel);
		}
	}

	private void putChannel(HostCredentialPair key, MetaChannel value) {
		if (logger.isDebugEnabled()) {
			logger.debug("putChannel(" + key + ", " + value + ")");
		}
		channels.put(key, value);
	}

	private MetaChannel getChannel(HostCredentialPair key) {
		MetaChannel meta =  channels.get(key);
		if (logger.isDebugEnabled()) {
			logger.debug("getChannel(" + key + "): " + meta);
		}
		return meta;
	}

	public void registerChannel(ChannelID id, CoasterChannel channel) throws ChannelException {
		if (channel == null) {
			throw new ChannelException("Cannot register null channel");
		}
		HostCredentialPair hcp = new HostCredentialPair("id://" + id.toString(), (GSSCredential) null);
		synchronized (channels) {
			MetaChannel previous = getChannel(hcp);
			if (previous != null) {
				if (logger.isInfoEnabled()) {
					logger.info("Re-registering " + id + " = " + channel);
				}
				try {
					/*
					 * Check to see if a rogue user is not trying to "steal" a
					 * channel
					 */

					if (!equals(getName(previous), getName(channel))) {
						throw new ChannelException("Channel registration denied. Expected name: "
								+ getName(previous) + "; actual name: " + getName(channel));
					}
				}
				catch (Exception e) {
					throw new ChannelException(e);
				}
				if (channel instanceof MetaChannel) {
					throw new ChannelException("Meta channel already exists");
				}
				else {
					previous.bind(channel);
					channel.setChannelContext(previous.getChannelContext());
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Registering " + id + " = " + channel);
				}
				if (channel instanceof MetaChannel) {
					putChannel(hcp, (MetaChannel) channel);
				}
				else {
					previous = new MetaChannel(channel.getRequestManager(),
							channel.getChannelContext());
					previous.bind(channel);
					putChannel(hcp, previous);
				}
			}
		}
	}

	private boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	private String getName(CoasterChannel channel) {
		UserContext uc = channel.getChannelContext().getUserContext();
		return uc == null ? null : uc.getName();
	}

	public CoasterChannel reserveChannel(String host, GSSCredential cred, RequestManager rm)
			throws ChannelException {
		MetaChannel channel = getClientChannel(host, cred, rm);
		reserveChannel(channel);
		return channel;
	}

	public CoasterChannel reserveChannel(String host, GSSCredential cred) throws ChannelException {
		MetaChannel channel = getClientChannel(host, cred, null);
		reserveChannel(channel);
		return channel;
	}

	public CoasterChannel reserveChannel(CoasterChannel channel) throws ChannelException {
		return reserveChannel(getMetaChannel(channel));
	}

	public CoasterChannel reserveChannel(ChannelContext context) throws ChannelException {
		return reserveChannel(getMetaChannel(context));
	}

	private void connect(MetaChannel meta) throws ChannelException {
		try {
			String contact = meta.getChannelContext().getRemoteContact();
			if (contact == null) {
				// Should buffer things for a certain period of time
				throw new ChannelException("Channel died and no contact available");
			}
			Client client = Client.newClient(contact, meta.getChannelContext(),
					clientRequestManager);
			putChannel(new HostCredentialPair("id://"
					+ client.getChannel().getChannelContext().getChannelID(),
					(GSSCredential) null), meta);
			meta.bind(client.getChannel());
		}
		catch (ChannelException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ChannelException(e);
		}
	}

	public CoasterChannel reserveChannel(MetaChannel meta) throws ChannelException {
		synchronized (meta) {
			meta.incUsageCount();
			if (meta.isOffline()) {
				connect(meta);
			}
		}
		return meta;
	}

	/**
	 * Returns <code>true</code> if this channel can still transmit after this
	 * exception
	 */
	public boolean handleChannelException(CoasterChannel channel, Exception e) {
		logger.info(channel + " handling channel exception", e == null ? new Throwable() : e);
		if (channel.isOffline()) {
			logger.info("Channel already shut down");
			return false;
		}
		channel.setLocalShutdown();
		ChannelContext ctx = channel.getChannelContext();
		RemoteConfiguration.Entry config = ctx.getConfiguration();
		boolean canContinue;
		try {
			if (config != null && config.hasOption(RemoteConfiguration.RECONNECT)) {
				buffer(channel);
				channel.close();
				asyncReconnect(channel, e);
				canContinue = true;
			}
			else {
				channel.close();
				shutdownChannel(channel);
				canContinue = false;
			}
		}
		catch (Exception e2) {
			logger.info("Failed to shut down channel", e2);
			canContinue = false;
		}
		ctx.channelShutDown(e);
		logger.info("Channel exception handled");
		return canContinue;
	}

	private void asyncReconnect(final CoasterChannel channel, final Exception e) {
		final ChannelContext ctx = channel.getChannelContext();
		final RemoteConfiguration.Entry config = ctx.getConfiguration();
		Thread t = new Thread() {
			public void run() {
				Exception ex = e;
				int limit = Integer.MAX_VALUE;
				if (config.hasArg(RemoteConfiguration.RECONNECT)) {
					limit = Integer.parseInt(config.getArg(RemoteConfiguration.RECONNECT));
				}
				while (ctx.getReconnectionAttempts() < limit) {

					try {
						connect(getMetaChannel(channel));
						ctx.setReconnectionAttempts(0);
						return;
					}
					catch (ChannelException e2) {
						ctx.setReconnectionAttempts(ctx.getReconnectionAttempts() + 1);
						ex = e2;
					}
					try {
						Thread.sleep((long) (1000 * Math.pow(2, ctx.getReconnectionAttempts())));
					}
					catch (InterruptedException e2) {
						channel.getChannelContext().getService().irrecoverableChannelError(channel,
								e2);
					}
				}
				channel.getChannelContext().getService().irrecoverableChannelError(channel, ex);
			}
		};
		t.setName("Reconnector");
		t.start();
	}

	public void releaseChannel(CoasterChannel channel) {
		if (channel == null) {
			return;
		}
		synchronized (channel) {
			int count = channel.decUsageCount();
			if (count == 0 && channel.isClient()) {
				RemoteConfiguration.Entry config = channel.getChannelContext().getConfiguration();
				if (config.hasOption(RemoteConfiguration.KEEPALIVE)) {
					if (config.hasArg(RemoteConfiguration.KEEPALIVE)) {
						String time = config.getArg(RemoteConfiguration.KEEPALIVE);
						int itime = Integer.parseInt(time);
						((MetaChannel) channel).deactivateLater(itime);
					}
				}
				else {
					unregisterChannel((MetaChannel) channel);
				}
			}
		}
	}

	private void registerChannel(HostCredentialPair key, MetaChannel channel) {
		synchronized (channels) {
			putChannel(key, channel);
			List<HostCredentialPair> l = rchannels.get(channel);
			if (l == null) {
				l = new LinkedList<HostCredentialPair>();
				rchannels.put(channel, l);
			}
			l.add(key);
		}
	}

	public void unregisterChannel(CoasterChannel channel) throws ChannelException {
		unregisterChannel(getMetaChannel(channel));
	}
	
	public void removeChannel(ChannelContext ctx) throws ChannelException {
	    removeChannel(ctx, true);
	}

	public void removeChannel(ChannelContext ctx, boolean unregister) throws ChannelException {
		if (ctx == null) {
			throw new NullPointerException("Null context");
		}
		if (unregister) {
			unregisterChannel(getMetaChannel(ctx));
		}
		synchronized (channels) {
			channels.remove(new HostCredentialPair("id://" + ctx.getChannelID(), (GSSCredential) null));
		}
	}
	
	protected void unregisterChannel(MetaChannel channel) {
		try {
			synchronized (channel) {
				RemoteConfiguration.Entry config = channel.getChannelContext().getConfiguration();
				if (config != null) {
					if (config.hasOption(RemoteConfiguration.BUFFER)) {
						channel.bind(new BufferingChannel(channel.getChannelContext()));
					}
					else if (config.hasOption(RemoteConfiguration.POLL)) {
						if (config.hasArg(RemoteConfiguration.POLL)) {
							String time = config.getArg(RemoteConfiguration.POLL);
							int itime = Integer.parseInt(time);
							channel.poll(itime);
						}
						else {
							channel.poll(300);
						}
						channel.bind(new NullChannel(true));
					}
					else {
						channel.bind(new NullChannel(true));
					}
				}
				else {
					channel.bind(new NullChannel(true));
				}
			}
		}
		catch (ChannelException e) {
			logger.error("Exception caught while unregistering channel", e);
		}
	}

	public void shutdownChannel(CoasterChannel channel) throws ChannelException {
		unregisterChannel(getMetaChannel(channel));
	}

	public void shutdownChannel(ChannelContext ctx) throws ChannelException {
		unregisterChannel(getMetaChannel(ctx));
	}

	private MetaChannel getMetaChannel(CoasterChannel channel) throws ChannelException {
		if (channel instanceof MetaChannel) {
			return (MetaChannel) channel;
		}
		return getMetaChannel(channel.getChannelContext());
	}

	private MetaChannel getMetaChannel(ChannelContext context) throws ChannelException {
		synchronized (channels) {
			if (logger.isDebugEnabled()) {
				logger.debug("\nLooking up " + context.getChannelID());
			}
			MetaChannel meta = null;
			HostCredentialPair hcp = new HostCredentialPair("id://"
					+ context.getChannelID(), (GSSCredential) null);
			meta = getChannel(hcp);

			if (meta == null && context.getRemoteContact() != null) {
				hcp = new HostCredentialPair(
						context.getRemoteContact(), context.getUserContext().getCredential());
				meta = getChannel(hcp);
			}

			if (meta == null) {
				throw new ChannelException("Invalid channel: " + hcp);
			}
			else {
				synchronized (meta) {
					if (logger.isDebugEnabled()) {
						logger.debug("Found      " + meta.getChannelContext().getChannelID());
					}
					if (context != meta.getChannelContext()) {
						logger.warn("Channels: " + channels);
						logger.warn("Context: " + context);
						logger.warn("Meta context: " + meta.getChannelContext());
						throw new ChannelException("Stored context is invalid");
					}
				}
				return meta;
			}
		}
	}

	public void reserveLongTerm(CoasterChannel channel) throws ChannelException {
		getMetaChannel(channel).incLongTermUsageCount();
	}

	public void releaseLongTerm(CoasterChannel channel) throws ChannelException {
		getMetaChannel(channel).decLongTermUsageCount();
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

	private void buffer(CoasterChannel channel) throws ChannelException {
		MetaChannel meta = getMetaChannel(channel);
		meta.bind(new BufferingChannel(channel.getChannelContext()));
	}
}
