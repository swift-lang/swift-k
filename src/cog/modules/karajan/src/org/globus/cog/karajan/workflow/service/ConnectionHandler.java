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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.GSSChannel;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.channels.StreamChannel;
import org.globus.cog.karajan.workflow.service.channels.TCPChannel;
import org.globus.gsi.gssapi.net.GssSocket;

public class ConnectionHandler {
	private static final Logger logger = Logger.getLogger(ConnectionHandler.class);

	private Socket socket;
	private final KarajanChannel channel;
	private final RequestManager requestManager;

	public ConnectionHandler(Service service, Socket socket) throws IOException {
		this(service, socket, null);
	}

	public ConnectionHandler(Service service, Socket socket, RequestManager requestManager) throws IOException {
	    this.requestManager = requestManager == null ? new ServiceRequestManager() : requestManager;
		this.socket = socket;
		
		if (socket instanceof GssSocket) {
			channel = new GSSChannel((GssSocket) socket, this.requestManager, new ChannelContext(
					service));
		}
		else {
			channel = new TCPChannel(socket, this.requestManager, new ChannelContext(service));
		}
	}
	
	protected ConnectionHandler(Socket socket, KarajanChannel channel, 
			RequestManager requestManager) throws IOException {
		assert requestManager != null;
		this.requestManager = requestManager;
        this.socket = socket;
        this.channel = channel;
	}
	
	public ConnectionHandler(Service service, InputStream is, OutputStream os, RequestManager requestManager) {
	    this.requestManager = requestManager == null ? new ServiceRequestManager() : requestManager;
	    channel = new StreamChannel(is, os, this.requestManager, new ChannelContext(service));
	}

	public void start() throws Exception {
	    if (socket != null) {
	    	socket.setKeepAlive(true);
	    	socket.setSoTimeout(0);
	    }
		channel.start();
    }
}
