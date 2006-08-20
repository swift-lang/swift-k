//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2006
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.io.IOException;
import java.net.Socket;

import org.globus.cog.karajan.workflow.service.ClientRequestManager;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.UserContext;

public class PlainSocketChannel extends AbstractSocketChannel {
	private UserContext uc;

	public PlainSocketChannel(String host, int port) throws IOException {
		this(new Socket(host, port), new ClientRequestManager(), new ChannelContext(), true);
		setEndpoint(host + ":" + port);
	}

	public PlainSocketChannel(Socket socket, RequestManager requestManager,
			ChannelContext channelContext, boolean client) {
		super(requestManager, channelContext, socket, client);
		uc = new UserContext(null, channelContext);
	}

	public UserContext getUserContext() {
		return uc;
	}

	public String toString() {
		return "SC-" + getEndpoint();
	}
}
