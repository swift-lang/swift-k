//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.coaster;

import org.globus.cog.coaster.handlers.ChannelConfigurationHandler;
import org.globus.cog.coaster.handlers.EchoHandler;
import org.globus.cog.coaster.handlers.HeartBeatHandler;
import org.globus.cog.coaster.handlers.ShutdownHandler;
import org.globus.cog.coaster.handlers.VersionHandler;

public class ServiceRequestManager extends AbstractRequestManager {
	public ServiceRequestManager() {
		addHandler("VERSION", VersionHandler.class);
		addHandler("CHANNELCONFIG", ChannelConfigurationHandler.class);
		addHandler("ECHO", EchoHandler.class);
		//addHandler("TEST", TestHandler.class);
		addHandler("SHUTDOWN", ShutdownHandler.class);
		addHandler(HeartBeatHandler.NAME, HeartBeatHandler.class);
	}
}
