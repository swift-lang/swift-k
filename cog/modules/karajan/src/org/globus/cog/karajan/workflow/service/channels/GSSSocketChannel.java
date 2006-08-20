//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 6, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.UserContext;
import org.globus.cog.karajan.workflow.service.commands.ShutdownCommand;
import org.globus.gsi.gssapi.net.GssSocket;

public class GSSSocketChannel extends AbstractSocketChannel implements Runnable {
	private static final Logger logger = Logger.getLogger(GSSSocketChannel.class);

	private final GssSocket socket;
	private String peerId;
	private UserContext uc;
	private boolean shuttingDown;
	private Exception startException;
	private final Replier replier;

	public GSSSocketChannel(GssSocket socket, RequestManager requestManager, ChannelContext sc,
			boolean client) {
		super(requestManager, sc, socket, client);
		this.socket = socket;
		replier = new Replier(this);
		EventBus.initialize();
	}
	
	public GSSSocketChannel(GssSocket socket, RequestManager requestManager, ChannelContext sc,
			boolean client, String endpoint) {
		this(socket, requestManager, sc, client);
		setEndpoint(endpoint);
	}

	public void sendTaggedReply(int tag, byte[] data, boolean fin, boolean err) {
		EventBus.post(replier, new ReplyEvent(tag, data, fin, err));
	}
	
	protected void initializeConnection() {
		try {
			if (socket.getContext().isEstablished()) {
				uc = getChannelContext().newUserContext(socket.getContext().getSrcName());
				// TODO Credentials should be associated with each
				// individual instance

				// X509Certificate[] chain = (X509Certificate[])
				// ((ExtendedGSSContext)
				// socket.getContext()).inquireByOid(GSSConstants.X509_CERT_CHAIN);
				if (socket.getContext().getCredDelegState()) {
					uc.setCredential(socket.getContext().getDelegCred());
				}
				peerId = uc.getName();
				logger.debug(getEndpoint() + "Peer identity: " + peerId);
			}
			else {
				throw new IOException("Context not established");
			}
		}
		catch (Exception e) {
			logger.warn(getEndpoint() + "Could not get client identity", e);
		}
	}

	public void shutdown() {
		if (isClosed()) {
			return;
		}
		synchronized (this) {
			if (isClosed()) {
				return;
			}
			if (!isLocalShutdown() && isClient()) {
				try {
					ShutdownCommand sc = new ShutdownCommand();
					logger.debug(getEndpoint() + "Initiating remote shutdown");
					sc.execute(this);
					logger.debug(getEndpoint() + "Remote shutdown ok");
				}
				catch (Exception e) {
					logger.warn(getEndpoint() + "Failed to shut down channel nicely", e);
				}
				super.shutdown();
				close();
			}
		}
	}

	public String getPeerId() {
		return peerId;
	}

	public UserContext getUserContext() {
		return uc;
	}
	
	public String toString() {
		return "GSSC-" + getEndpoint();
	}
}
