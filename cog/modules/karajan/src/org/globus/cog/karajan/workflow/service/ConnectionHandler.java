//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 19, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.net.Socket;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.channels.AbstractSocketChannel;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.GSSSocketChannel;
import org.globus.cog.karajan.workflow.service.channels.PlainSocketChannel;
import org.globus.gsi.gssapi.net.GssSocket;

public class ConnectionHandler {
	private static final Logger logger = Logger.getLogger(ConnectionHandler.class);

	private final Socket socket;
	private final AbstractSocketChannel channel;
	private final RequestManager requestManager;

	public ConnectionHandler(Service service, Socket socket) {
		this.socket = socket;
		requestManager = new ServiceRequestManager();
		if (socket instanceof GssSocket) {
			channel = new GSSSocketChannel((GssSocket) socket, requestManager, new ChannelContext(
					service), false);
		}
		else {
			channel = new PlainSocketChannel(socket, requestManager, new ChannelContext(service),
					false);
		}
	}

	public void start() throws Exception {
		socket.setKeepAlive(true);
		socket.setSoTimeout(0);
		channel.start();
	}
}
