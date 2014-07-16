//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 14, 2008
 */
package org.globus.cog.coaster;

import java.net.URI;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.coaster.channels.ChannelContext;
import org.globus.cog.coaster.channels.ChannelException;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.GSSChannel;
import org.globus.cog.coaster.channels.TCPChannel;
import org.globus.cog.coaster.channels.UDPChannel;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

public class ChannelFactory {
	public static final Logger logger = Logger.getLogger(ChannelFactory.class);

	public static final int DEFAULT_CREDENTIAL_REFRESH_INTERVAL = 30000;
	private static GSSCredential cachedCredential;
	private static long credentialTime;

	public static CoasterChannel newChannel(URI contact, ChannelContext context, RequestManager rm)
			throws ChannelException {
		CoasterChannel channel;
		try {
			if (contact.getScheme() == null || contact.getScheme().equals("tcps")) {
				ensureCredentialPresent(context);
				channel = new GSSChannel(contact, rm, context);
			}
			else if (contact.getScheme().equals("https")) {
				ensureCredentialPresent(context);
				channel = new GSSChannel(contact, rm, context);
			}
			else if (contact.getScheme().equals("tcp") || contact.getScheme().equals("http")) {
				channel = new TCPChannel(contact, context, rm);
			}
			else if (contact.getScheme().equals("udp")) {
				channel = new UDPChannel(contact, context, rm);
			}
			else {
				throw new IllegalArgumentException("Scheme not supported: " + contact);
			}
			channel.start();
			return channel;
		}
		catch (InvalidSecurityContextException e) {
			throw new ChannelException(e);
		}
	}

	private static void ensureCredentialPresent(ChannelContext context)
			throws InvalidSecurityContextException {
		if (context.getUserContext().getCredential() == null) {
			context.getUserContext().setCredential(getDefaultCredential());
		}
	}

	public static GSSCredential getDefaultCredential() throws InvalidSecurityContextException {
		synchronized (ChannelFactory.class) {
			if (cachedCredential == null
					||
					(System.currentTimeMillis() - credentialTime) > DEFAULT_CREDENTIAL_REFRESH_INTERVAL) {
				credentialTime = System.currentTimeMillis();
				GSSManager manager = ExtendedGSSManager.getInstance();
				try {
					cachedCredential = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
				}
				catch (GSSException e) {
					throw new InvalidSecurityContextException(e);
				}
			}
			return cachedCredential;
		}
	}
}
