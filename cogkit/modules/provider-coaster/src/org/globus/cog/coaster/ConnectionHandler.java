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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;
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
			channel = new GSSChannel((GssSocket) socket, this.requestManager, null);
		}
		else {
			channel = new TCPChannel(socket, this.requestManager, null);
		}
		channel.setService(service);
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
	    channel = new StreamChannel(is, os, this.requestManager, null, false);
	}

	public void start() throws Exception {
	    if (socket != null) {
	    	socket.setKeepAlive(true);
	    	socket.setSoTimeout(0);
	    }
		channel.start();
    }
}
