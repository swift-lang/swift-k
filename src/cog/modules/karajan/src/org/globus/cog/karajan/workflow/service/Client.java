//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 19, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.GSSSocketChannel;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.ChannelConfigurationCommand;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.VersionCommand;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.GlobusGSSManagerImpl;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.globus.gsi.gssapi.auth.SelfAuthorization;
import org.globus.gsi.gssapi.net.GssSocket;
import org.globus.gsi.gssapi.net.GssSocketFactory;
import org.gridforum.jgss.ExtendedGSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;

public class Client {
	private static final Logger logger = Logger.getLogger(Client.class);

	private final URI contact;
	private Socket socket;
	private GSSSocketChannel channel;
	private RequestManager requestManager;
	private ChannelContext sc;
	private static Service callback;
	private boolean connected, connecting;
	private Exception e;

	private static Map clients = new Hashtable();
	private static Service service;

	public static Client getClient(String contact) throws Exception {
		Client client;
		synchronized (clients) {
			if (!clients.containsKey(contact)) {
				client = new Client(contact);
				clients.put(contact, client);
			}
			client = (Client) clients.get(contact);
		}
		try {
			synchronized (client) {
				client.connect();
			}
		}
		catch (Exception e) {
			synchronized (clients) {
				clients.remove(contact);
			}
			throw e;
		}
		return client;
	}

	public static Client newClient(String contact, ChannelContext context) throws Exception {
		Client client = new Client(contact);
		client.setChannelContext(context);
		context.getChannelID().setClient(true);
		synchronized (client) {
			client.connect();
		}
		return client;
	}

	private void setChannelContext(ChannelContext context) {
		this.sc = context;
	}

	public Client(String contact) throws URISyntaxException {
		this.contact = new URI(contact);
	}

	public void connect() throws Exception {
		synchronized (this) {
			while (connecting) {
				wait();
			}
			if (e != null) {
				throw e;
			}
			if (connected) {
				return;
			}
			else {
				connecting = true;
			}
		}
		try {
			if (sc == null) {
				sc = new ChannelContext();
				sc.setConfiguration(RemoteConfiguration.getDefault().find(contact.toString()));
			}
			String host = contact.getHost();
			int port = contact.getPort();
			if (port == -1) {
				port = 1984;
			}
			HostAuthorization hostAuthz = new HostAuthorization("host");

			Authorization authz = new FallbackAuthorization(new Authorization[] { hostAuthz,
					SelfAuthorization.getInstance() });


			GSSCredential cred = sc.getCredential();

			GSSManager manager = new GlobusGSSManagerImpl();
			ExtendedGSSContext context = (ExtendedGSSContext) manager.createContext(null,
					GSSConstants.MECH_OID, cred, 2 * 3600);

			context.requestAnonymity(false);
			context.requestCredDeleg(false);
			context.setOption(GSSConstants.GSS_MODE, GSIConstants.MODE_SSL);
			context.setOption(GSSConstants.DELEGATION_TYPE, GSIConstants.DELEGATION_TYPE_LIMITED);

			socket = GssSocketFactory.getDefault().createSocket(host, port, context);
			socket.setKeepAlive(true);
			socket.setSoTimeout(0);
			((GssSocket) socket).setWrapMode(GSIConstants.MODE_SSL.intValue());
			((GssSocket) socket).setAuthorization(authz);
			requestManager = new ClientRequestManager();
			logger.info("Connected to " + host + ":" + port);

			sc.setRemoteContact(contact.toString());
			channel = new GSSSocketChannel((GssSocket) socket, requestManager, sc, true,
					contact.toString());
			channel.start();

			String callbackURL = null;
			if (sc.getConfiguration().hasOption(RemoteConfiguration.CALLBACK)) {
				callbackURL = ChannelManager.getManager().getCallbackURL();
			}
			String remoteID = getChannel().getChannelContext().getChannelID().getRemoteID();

			ChannelConfigurationCommand ccc = new ChannelConfigurationCommand(
					sc.getConfiguration(), callbackURL);
			ccc.execute(this.getChannel());
			connected = true;
		}
		catch (Exception e) {
			this.e = e;
			throw e;
		}
		finally {
			synchronized (this) {
				connecting = false;
				notifyAll();
			}
		}
	}

	public void execute(Command command) throws IOException, ProtocolException {
		command.execute(channel);
	}

	public void executeAsync(Command command) throws ProtocolException, IOException {
		command.executeAsync(channel);
	}

	public void close() throws IOException {
		channel.close();
	}

	public KarajanChannel getChannel() {
		return channel;
	}

	public static void main(String[] args) {
		try {
			Client client = new Client("https://localhost:50000");
			client.connect();
			VersionCommand ver = new VersionCommand();
			client.execute(ver);
			System.out.println("Server version: " + ver.getServerVersion());
			client.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
