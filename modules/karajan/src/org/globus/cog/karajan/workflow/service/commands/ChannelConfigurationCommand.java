//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.karajan.workflow.service.commands;

import java.rmi.server.UID;

import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RemoteConfiguration;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelID;


public class ChannelConfigurationCommand extends Command {
	private final RemoteConfiguration.Entry config;
	private final String callbackURL;
	
	public ChannelConfigurationCommand(RemoteConfiguration.Entry config, String callbackURL) {
		super("CHANNELCONFIG");
		this.config = config;
		this.callbackURL = callbackURL;
	}

	public void send() throws ProtocolException {
		addOutData(config.getUnparsed());
		if (callbackURL == null) {
			addOutData(new byte[0]);
		}
		else {
			addOutData(callbackURL.getBytes());
		}
		ChannelContext cc = getChannel().getChannelContext();
		ChannelID cid = cc.getChannelID();
		if (cid.getLocalID() == null) {
			cid.setLocalID(new UID().toString());
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
