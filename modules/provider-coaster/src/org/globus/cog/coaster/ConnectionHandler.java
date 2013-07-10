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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.channels.ChannelContext;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.GSSChannel;
import org.globus.cog.coaster.channels.StreamChannel;
import org.globus.cog.coaster.channels.TCPChannel;
import org.globus.gsi.gssapi.net.GssSocket;

public class ConnectionHandler {
	private static final Logger logger = Logger.getLogger(ConnectionHandler.class);

	private Socket socket;
	private final CoasterChannel channel;
	private final RequestManager requestManager;

	public ConnectionHandler(String name, Service service, Socket socket) throws IOException {
		this(name, service, socket, null);
	}

	public ConnectionHandler(String name, Service service, Socket socket, RequestManager requestManager) 
	throws IOException {
	    this.requestManager = requestManager == null ? new ServiceRequestManager() : requestManager;
		this.socket = socket;
		
		if (socket instanceof GssSocket) {
			channel = new GSSChannel((GssSocket) socket, this.requestManager, new ChannelContext(name, service));
		}
		else {
			channel = new TCPChannel(socket, this.requestManager, new ChannelContext(name, service));
		}
	}
	
	protected ConnectionHandler(Socket socket, CoasterChannel channel, 
			RequestManager requestManager) throws IOException {
		assert requestManager != null;
		this.requestManager = requestManager;
        this.socket = socket;
        this.channel = channel;
	}
	
	public ConnectionHandler(String name, Service service, InputStream is, OutputStream os, RequestManager requestManager) {
	    this.requestManager = requestManager == null ? new ServiceRequestManager() : requestManager;
	    channel = new StreamChannel(is, os, this.requestManager, new ChannelContext(name, service));
	}

	public void start() throws Exception {
	    if (socket != null) {
	    	socket.setKeepAlive(true);
	    	socket.setSoTimeout(0);
	    }
		channel.start();
    }
}
