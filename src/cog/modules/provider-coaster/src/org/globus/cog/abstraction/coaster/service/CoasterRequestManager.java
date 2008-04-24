//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 19, 2008
 */
package org.globus.cog.abstraction.coaster.service;

import org.globus.cog.abstraction.impl.execution.coaster.SubmitJobCommand;
import org.globus.cog.karajan.workflow.service.AbstractRequestManager;
import org.globus.cog.karajan.workflow.service.handlers.ChannelConfigurationHandler;
import org.globus.cog.karajan.workflow.service.handlers.ShutdownHandler;
import org.globus.cog.karajan.workflow.service.handlers.VersionHandler;

public class CoasterRequestManager extends AbstractRequestManager {   
    public CoasterRequestManager() {
        addHandler("VERSION", VersionHandler.class);
        addHandler("CHANNELCONFIG", ChannelConfigurationHandler.class);
        addHandler("SHUTDOWN", ShutdownHandler.class);
        addHandler(SubmitJobCommand.NAME, SubmitJobHandler.class);
        addHandler(ServiceShutdownHandler.NAME, ServiceShutdownHandler.class);
    }
}
