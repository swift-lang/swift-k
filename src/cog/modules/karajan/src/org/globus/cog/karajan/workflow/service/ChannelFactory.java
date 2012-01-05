//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 14, 2008
 */
package org.globus.cog.karajan.workflow.service;

import java.net.URI;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelException;
import org.globus.cog.karajan.workflow.service.channels.GSSChannel;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.channels.TCPChannel;
import org.globus.cog.karajan.workflow.service.channels.UDPChannel;

public class ChannelFactory {
	public static final Logger logger = Logger.getLogger(ChannelFactory.class);

	public static KarajanChannel newChannel(URI contact, ChannelContext context, RequestManager rm)
			throws ChannelException {
		KarajanChannel channel;
		if (contact.getScheme() == null || contact.getScheme().equals("tcps")) {
			channel = new GSSChannel(contact, rm, context);
		}
		else if (contact.getScheme().equals("https")) {
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
}
