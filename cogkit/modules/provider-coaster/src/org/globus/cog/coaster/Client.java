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
 * Created on Jul 19, 2005
 */
package org.globus.cog.coaster;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.VersionCommand;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;

public class Client {
	private static final Logger logger = Logger.getLogger(Client.class);

	private final URI contact;
	private CoasterChannel channel;
	private RequestManager requestManager;
	private UserContext userContext;
	private static Service callback;
	private boolean connected, connecting;
	private Exception e;

	private static Map<String, Client> clients = new HashMap<String, Client>();
	private static Service service;

	public static Client getClient(String contact, UserContext userContext, RequestManager rm) throws Exception {
		Client client;
		synchronized (clients) {
			if (!clients.containsKey(contact)) {
				client = newClient(contact, userContext, rm);
				clients.put(contact, client);
			}
			client = clients.get(contact);
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

	public static Client newClient(String contact, UserContext userContext,
			RequestManager requestManager) throws Exception {
		Client client = new Client(contact, userContext, requestManager);
		synchronized (client) {
			client.connect();
		}
		return client;
	}

	public Client(String contact) throws URISyntaxException {
		this(contact, new UserContext(), new ClientRequestManager());
	}

	public Client(String contact, UserContext userContext, RequestManager requestManager) throws URISyntaxException {
		this.contact = new URI(contact);
		this.requestManager = requestManager;
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
			URI c = contact;
			String host = contact.getHost();
			int port = contact.getPort();
			if (port == -1) {
				c = new URI(contact.getScheme(), null, contact.getHost(), 1984, null, null, null);
			}
			channel = ChannelFactory.newChannel(c, userContext, requestManager);
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

	public void execute(Command command) throws IOException, ProtocolException, InterruptedException {
		command.execute(channel);
	}

	public void executeAsync(Command command) throws ProtocolException, IOException {
		command.executeAsync(channel);
	}

	public void close() throws IOException {
		channel.close();
	}

	public CoasterChannel getChannel() {
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
