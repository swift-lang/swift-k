//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.karajan.workflow.service;

import org.globus.cog.karajan.workflow.service.handlers.ChannelConfigurationHandler;
import org.globus.cog.karajan.workflow.service.handlers.EchoHandler;
import org.globus.cog.karajan.workflow.service.handlers.EventHandler;
import org.globus.cog.karajan.workflow.service.handlers.ShutdownHandler;
import org.globus.cog.karajan.workflow.service.handlers.StartGroupHandler;
import org.globus.cog.karajan.workflow.service.handlers.StartHandler;
import org.globus.cog.karajan.workflow.service.handlers.SubmitHandler;
import org.globus.cog.karajan.workflow.service.handlers.UploadHandler;
import org.globus.cog.karajan.workflow.service.handlers.VargHandler;
import org.globus.cog.karajan.workflow.service.handlers.VersionHandler;
import org.globus.cog.karajan.workflow.service.management.handlers.StatHandler;

public class ServiceRequestManager extends AbstractRequestManager {
	public ServiceRequestManager() {
		addHandler("VERSION", VersionHandler.class);
		addHandler("UPLOAD", UploadHandler.class);
		addHandler("START", StartHandler.class);
		addHandler("CHANNELCONFIG", ChannelConfigurationHandler.class);
		addHandler("STARTGROUP", StartGroupHandler.class);
		addHandler("ECHO", EchoHandler.class);
		//addHandler("TEST", TestHandler.class);
		addHandler("EVENT", EventHandler.class);
		addHandler("VARG", VargHandler.class);
		addHandler("SHUTDOWN", ShutdownHandler.class);
		addHandler("STAT", StatHandler.class);
		addHandler("SUBMIT", SubmitHandler.class);
	}
}
