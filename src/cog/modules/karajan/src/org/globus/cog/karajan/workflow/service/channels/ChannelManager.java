//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 22, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.Client;
import org.globus.cog.karajan.workflow.service.ClientRequestManager;
import org.globus.cog.karajan.workflow.service.RemoteConfiguration;
import org.globus.cog.karajan.workflow.service.Service;
import org.globus.gsi.GlobusCredentialException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class ChannelManager {
	private static final Logger logger = Logger.getLogger(ChannelManager.class);

	private static ChannelManager manager;
	private HashMap channels, hosts, rchannels;
	private RemoteConfiguration config;
	private Service localService;

	public synchronized static ChannelManager getManager() {
		if (manager == null) {
			manager = new ChannelManager();
		}
		return manager;
	}

	public ChannelManager() {
		channels = new HashMap();
		hosts = new HashMap();
		rchannels = new HashMap();
		config = RemoteConfiguration.getDefault();
	}

	private MetaChannel getClientChannel(String host, GSSCredential cred) throws ChannelException {
		try {
			MetaChannel channel;
			host = normalize(host);
			synchronized (channels) {
				HostCredentialPair hcp = new HostCredentialPair(host, cred);
				channel = (MetaChannel) channels.get(hcp);
				if (channel == null) {
					channel = new MetaChannel(new ClientRequestManager(), new ChannelContext());
					channel.getChannelContext().setConfiguration(
							RemoteConfiguration.getDefault().find(host));
					channel.getChannelContext().setRemoteContact(host);
					channel.getChannelContext().setCredential(cred);
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

	public synchronized String getCallbackURL() throws IOException, GlobusCredentialException,
			GSSException {
		return getLocalService().getContactString();
	}

	public synchronized Service getLocalService() throws IOException, GlobusCredentialException,
			GSSException {
		if (localService == null) {
			localService = new Service(Service.initializeCredentials(true, null, null));
		}
		logger.info("Started local service: " + localService.getContactString());
		return localService;
	}

	public void registerChannel(ChannelID id, KarajanChannel channel) throws ChannelException {
		if (channel == null) {
			throw new ChannelException("Cannot register null channel");
		}
		synchronized (channels) {
			MetaChannel previous = (MetaChannel) channels.get(id);
			if (previous != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Re-registering " + id + " = " + channel);
				}
				try {
					/*
					 * Check to see if a rogue user is not trying to "steal" a
					 * channel
					 */
					if (!previous.getChannelContext().getUserContext().getName().equals(
							channel.getChannelContext().getUserContext().getName())) {
						throw new ChannelException("Channel registration denied. Expected name: "
								+ previous.getChannelContext().getUserContext().getName()
								+ "; actual name: "
								+ channel.getChannelContext().getUserContext().getName());
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
					previous = new MetaChannel(channel.getChannelContext());
					previous.bind(channel);
					channels.put(id, previous);
				}
			}
		}
	}

	public KarajanChannel reserveChannel(String host, GSSCredential cred) throws ChannelException {
		MetaChannel channel = getClientChannel(host, cred);
		reserveChannel(channel);
		return channel;
	}

	public KarajanChannel reserveChannel(KarajanChannel channel) throws ChannelException {
		return reserveChannel(getChannel(channel));
	}

	public KarajanChannel reserveChannel(ChannelContext context) throws ChannelException {
		return reserveChannel(getChannel(context));
	}

	public KarajanChannel reserveChannel(MetaChannel meta) throws ChannelException {
		synchronized (meta) {
			meta.incUsageCount();
			RemoteConfiguration.Entry config = meta.getChannelContext().getConfiguration();
			if (meta.isOffline()) {
				try {
					String contact = meta.getChannelContext().getRemoteContact();
					if (contact == null) {
						//Should buffer things for a certain period of time
						throw new ChannelException("Channel died and no contact available");
					}
					Client client = Client.newClient(contact, meta.getChannelContext());
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
		}
		return meta;
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
					try {
						unregisterChannel((MetaChannel) channel);
					}
					catch (ChannelException e) {
						logger.warn("Exception caught while unregistering channel", e);
					}
				}
			}
		}
	}

	private void registerChannel(Object key, KarajanChannel channel) {
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

	protected void unregisterChannel(MetaChannel channel) throws ChannelException {
		synchronized (channel) {
			RemoteConfiguration.Entry config = channel.getChannelContext().getConfiguration();
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
	}

	public void shutdownChannel(KarajanChannel channel) throws ChannelException {
		unregisterChannel(getChannel(channel));
	}

	private MetaChannel getChannel(KarajanChannel channel) throws ChannelException {
		if (channel instanceof MetaChannel) {
			return (MetaChannel) channel;
		}
		return getChannel(channel.getChannelContext());
	}

	private MetaChannel getChannel(ChannelContext context) throws ChannelException {
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
		getChannel(channel).incLongTermUsageCount();
	}

	public void releaseLongTerm(KarajanChannel channel) throws ChannelException {
		getChannel(channel).decLongTermUsageCount();
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

	}
}
