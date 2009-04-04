//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 22, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.Client;
import org.globus.cog.karajan.workflow.service.ClientRequestManager;
import org.globus.cog.karajan.workflow.service.RemoteConfiguration;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.Service;
import org.globus.cog.karajan.workflow.service.UserContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class ChannelManager {
	private static final Logger logger = Logger.getLogger(ChannelManager.class);

	private static ChannelManager manager;
	private HashMap channels, hosts, rchannels;
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
		channels = new HashMap();
		hosts = new HashMap();
		rchannels = new HashMap();
		config = RemoteConfiguration.getDefault();
		clientRequestManager = new ClientRequestManager();
	}

	protected void setClientRequestManager(RequestManager crm) {
		this.clientRequestManager = crm;
	}

	public KarajanChannel getExistingChannel(String host, GSSCredential cred) {
		MetaChannel channel;
		if (host == null) {
			throw new NullPointerException("Host is null");
		}
		host = normalize(host);
		synchronized (channels) {
			HostCredentialPair hcp = new HostCredentialPair(host, cred);
			channel = (MetaChannel) channels.get(hcp);
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
				channel = (MetaChannel) channels.get(hcp);
				if (channel == null) {
					ChannelContext context = new ChannelContext();
					context.setConfiguration(RemoteConfiguration.getDefault().find(host));
					context.setRemoteContact(host);
					context.setCredential(cred);
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
		StringBuffer sb = new StringBuffer();
		int pi = url.indexOf("://");
		if (pi == -1) {
			sb.append("https://");
			pi = -3;
		}
		else {
			sb.append(url.substring(0, pi + 3));
		}
		sb.append(url.substring(pi + 3));
		int hi = url.indexOf(':', pi + 3);
		if (hi == -1) {
			sb.append(":1984");
		}
		return sb.toString();
	}

	public void registerChannel(String url, GSSCredential cred, KarajanChannel channel)
			throws ChannelException {
		synchronized (channels) {
			HostCredentialPair hcp = new HostCredentialPair(url, cred);
			MetaChannel previous = getMetaChannel(channel);
			if (previous == null) {
				previous = new MetaChannel(channel.getRequestManager(), channel.getChannelContext());
			}
			channels.put(hcp, previous);
			previous.bind(channel);
		}
	}

	public void registerChannel(ChannelID id, KarajanChannel channel) throws ChannelException {
		if (channel == null) {
			throw new ChannelException("Cannot register null channel");
		}
		synchronized (channels) {
			MetaChannel previous = (MetaChannel) channels.get(id);
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
					channels.put(id, channel);
				}
				else {
					previous = new MetaChannel(channel.getRequestManager(),
							channel.getChannelContext());
					previous.bind(channel);
					channels.put(id, previous);
				}
			}
		}
	}

	private boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	private String getName(KarajanChannel channel) {
		UserContext uc = channel.getChannelContext().getUserContext();
		return uc == null ? null : uc.getName();
	}

	public KarajanChannel reserveChannel(String host, GSSCredential cred, RequestManager rm)
			throws ChannelException {
		MetaChannel channel = getClientChannel(host, cred, rm);
		reserveChannel(channel);
		return channel;
	}

	public KarajanChannel reserveChannel(String host, GSSCredential cred) throws ChannelException {
		MetaChannel channel = getClientChannel(host, cred, null);
		reserveChannel(channel);
		return channel;
	}

	public KarajanChannel reserveChannel(KarajanChannel channel) throws ChannelException {
		return reserveChannel(getMetaChannel(channel));
	}

	public KarajanChannel reserveChannel(ChannelContext context) throws ChannelException {
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
			channels.put(client.getChannel().getChannelContext().getChannelID(), meta);
			meta.bind(client.getChannel());

		}
		catch (ChannelException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ChannelException(e);
		}
	}

	public KarajanChannel reserveChannel(MetaChannel meta) throws ChannelException {
		synchronized (meta) {
			meta.incUsageCount();
			RemoteConfiguration.Entry config = meta.getChannelContext().getConfiguration();
			if (meta.isOffline()) {
				connect(meta);
			}
		}
		return meta;
	}

	public void handleChannelException(KarajanChannel channel, Exception e) {
		logger.info("Handling channel exception", e == null ? new Throwable() : e);
		if (channel.isOffline()) {
			logger.info("Channel already shut down");
			return;
		}
		channel.setLocalShutdown();
		ChannelContext ctx = channel.getChannelContext();
		RemoteConfiguration.Entry config = ctx.getConfiguration();
		try {
			if (config != null) {
				if (config.hasOption(RemoteConfiguration.RECONNECT)) {
					buffer(channel);
					channel.close();
					asyncReconnect(channel, e);
				}
				else {
					shutdownChannel(channel);
				}
			}
			else {
				shutdownChannel(channel);
			}
		}
		catch (Exception e2) {
			logger.warn("Failed to shut down channel", e2);
		}
		logger.info("Channel exception handled");
	}

	private void asyncReconnect(final KarajanChannel channel, final Exception e) {
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

	public void releaseChannel(KarajanChannel channel) {
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

	private void registerChannel(Object key, MetaChannel channel) {
		synchronized (channels) {
			channels.put(key, channel);
			List l = (List) rchannels.get(channel);
			if (l == null) {
				l = new LinkedList();
				rchannels.put(channel, l);
			}
			l.add(key);
		}
	}

	public void unregisterChannel(KarajanChannel channel) throws ChannelException {
		unregisterChannel(getMetaChannel(channel));
	}
	
	public void removeChannel(ChannelContext ctx) throws ChannelException {
	    if (ctx == null) {
	        throw new NullPointerException("Null context");
	    }
        unregisterChannel(getMetaChannel(ctx));
        synchronized(channels) {
            channels.remove(ctx.getChannelID());
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
						channel.bind(new NullChannel());
					}
					else {
						channel.bind(new NullChannel());
					}
				}
				else {
					channel.bind(new NullChannel());
				}
			}
		}
		catch (ChannelException e) {
			logger.error("Exception caught while unregistering channel", e);
		}
	}

	public void shutdownChannel(KarajanChannel channel) throws ChannelException {
		unregisterChannel(getMetaChannel(channel));
	}
	
	public void shutdownChannel(ChannelContext ctx) throws ChannelException {
        unregisterChannel(getMetaChannel(ctx));
    }

	private MetaChannel getMetaChannel(KarajanChannel channel) throws ChannelException {
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
			meta = (MetaChannel) channels.get(context.getChannelID());

			if (meta == null && context.getRemoteContact() != null) {
				meta = (MetaChannel) channels.get(new HostCredentialPair(
						context.getRemoteContact(), context.getCredential()));
			}

			if (meta == null) {
				throw new ChannelException("Invalid channel: " + context);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Found      " + meta.getChannelContext().getChannelID());
				}
				if (context != meta.getChannelContext()) {
					throw new ChannelException("Stored context is invalid");
				}
				return meta;
			}
		}
	}

	public void reserveLongTerm(KarajanChannel channel) throws ChannelException {
		getMetaChannel(channel).incLongTermUsageCount();
	}

	public void releaseLongTerm(KarajanChannel channel) throws ChannelException {
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
				return host.equals(other.host)
						&& ((DN == null && other.DN == null) || DN.equals(other.DN));
			}
			return false;
		}

		public int hashCode() {
			return host.hashCode() + ((DN == null) ? 0 : DN.hashCode());
		}

		public String toString() {
			return DN + "@" + host;
		}

	}

	private void buffer(KarajanChannel channel) throws ChannelException {
		MetaChannel meta = getMetaChannel(channel);
		meta.bind(new BufferingChannel(channel.getChannelContext()));
	}
}
