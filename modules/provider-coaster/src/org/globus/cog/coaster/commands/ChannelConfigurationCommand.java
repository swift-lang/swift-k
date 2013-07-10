//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.coaster.commands;

import java.net.URI;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.RemoteConfiguration;
import org.globus.cog.coaster.channels.ChannelContext;
import org.globus.cog.coaster.channels.ChannelID;


public class ChannelConfigurationCommand extends Command {
	public static final Logger logger = Logger.getLogger(ChannelConfigurationCommand.class);
	
	private final RemoteConfiguration.Entry config;
	private final URI callbackURI;
	
	public ChannelConfigurationCommand(RemoteConfiguration.Entry config, URI callbackURI) {
		super("CHANNELCONFIG");
		this.config = config;
		this.callbackURI = callbackURI;
	}

	public void send() throws ProtocolException {
		addOutData(config.getUnparsed());
		if (callbackURI == null) {
			addOutData(new byte[0]);
		}
		else {
			addOutData(callbackURI.toString().getBytes());
		}
		ChannelContext cc = getChannel().getChannelContext();
		ChannelID cid = cc.getChannelID();
		if (cid.getLocalID() == null) {
			cid.setLocalID(ChannelID.newUID());
		}
		addOutID(cid.getLocalID());
		addOutID(cid.getRemoteID());
		super.send();
	}
	
	private static final byte[] EMPTYBSTR = new byte[0];
	
	private void addOutID(String id) {
		if (id == null) {
			addOutData(EMPTYBSTR);
		}
		else {
			addOutData(id.getBytes());
		}
	}

	public void receiveCompleted() {
		if (logger.isInfoEnabled()) {
			logger.info("Got reply");
		}
		ChannelID cid = getChannel().getChannelContext().getChannelID();
		if (cid.getRemoteID() == null) {
			cid.setRemoteID(new String(getInData(0)));
		}
		super.receiveCompleted();
	}

	public RemoteConfiguration.Entry getConfig() {
		return config;
	}
}
