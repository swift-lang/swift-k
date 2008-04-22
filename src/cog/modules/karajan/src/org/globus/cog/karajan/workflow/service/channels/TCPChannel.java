//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2006
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.net.Socket;
import java.net.URI;

import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.UserContext;

public class TCPChannel extends AbstractTCPChannel {
	private UserContext uc;
	private URI contact;

	public TCPChannel(URI contact, ChannelContext context, RequestManager rm) {
		super(rm, context, true);
		this.contact = contact;
		setName(contact.toString());
	}

	public TCPChannel(Socket socket, RequestManager requestManager, ChannelContext channelContext) {
		super(requestManager, channelContext, false);
		setSocket(socket);
		uc = new UserContext(null, channelContext);
	}

	public void start() throws ChannelException {
		try {
			if (contact != null) {
				setSocket(new Socket(contact.getHost(), contact.getPort()));
			}
		}
		catch (Exception e) {
			throw new ChannelException("Failed to create socket", e);
		}
		super.start();
	}

	public UserContext getUserContext() {
		return uc;
	}

	public String toString() {
		return "SC-" + getContact();
	}
}
