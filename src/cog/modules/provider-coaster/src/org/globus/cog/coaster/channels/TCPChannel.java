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
	private URI contact;

	public TCPChannel(URI contact, ChannelContext channelContext, RequestManager rm) {
		super(rm, channelContext, true);
		this.contact = contact;
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
		return "TCP-" + getContact();
	}
}
