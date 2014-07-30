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
 * Created on Jul 21, 2006
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.channels.SocketChannel;

import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.UserContext;

public class TCPChannel extends AbstractTCPChannel {
	
	public TCPChannel(URI contact, ChannelContext channelContext, RequestManager rm) {
		super(rm, channelContext, true);
		setContact(contact);
		setName(contact.toString());
		channelContext.setUserContext(new UserContext(channelContext));
	}

	public TCPChannel(Socket socket, RequestManager requestManager, ChannelContext channelContext)
			throws IOException {
		super(requestManager, channelContext, false);
		setSocket(socket);
		channelContext.setUserContext(new UserContext(channelContext));
	}

	public void start() throws ChannelException {
		reconnect();
		super.start();
	}

	protected void reconnect() throws ChannelException {
		try {
			URI contact = getContact();
			if (contact != null) {
				SocketChannel chan = SocketChannel.open(new InetSocketAddress(contact.getHost(), contact.getPort()));
				setSocket(chan.socket());
			}
		}
		catch (Exception e) {
			throw new ChannelException("Failed to create socket", e);
		}
	}

	public String toString() {
		return "TCPChannel [type: " + (isClient() ? "client" : "server") + ", contact: " + getPeerName() + "]";
	}

	private String getPeerName() {
		if (getContact() != null) {
			return getContact().toString();
		}
		Socket sock = getSocket();
		if (sock != null) {
			return sock.getInetAddress().getHostAddress() + ":" + sock.getPort();
		}
		return "unknown";
	}
}
