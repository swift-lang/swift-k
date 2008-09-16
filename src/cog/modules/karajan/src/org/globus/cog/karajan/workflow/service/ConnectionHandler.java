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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.channels.AbstractTCPChannel;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.GSSChannel;
import org.globus.cog.karajan.workflow.service.channels.TCPChannel;
import org.globus.gsi.gssapi.net.GssSocket;

public class ConnectionHandler {
	private static final Logger logger = Logger.getLogger(ConnectionHandler.class);

	private final Socket socket;
	private final AbstractTCPChannel channel;
	private final RequestManager requestManager;

	public ConnectionHandler(Service service, Socket socket) throws IOException {
		this(service, socket, null);
	}

	public ConnectionHandler(Service service, Socket socket, RequestManager requestManager) throws IOException {
		this.socket = socket;
		this.requestManager = requestManager == null ? new ServiceRequestManager() : requestManager;
		if (socket instanceof GssSocket) {
			channel = new GSSChannel((GssSocket) socket, this.requestManager, new ChannelContext(
					service));
		}
		else {
			channel = new TCPChannel(socket, this.requestManager, new ChannelContext(service));
		}
	}

	public void start() throws Exception {
		socket.setKeepAlive(true);
		socket.setSoTimeout(0);
		channel.start();
    }
}
