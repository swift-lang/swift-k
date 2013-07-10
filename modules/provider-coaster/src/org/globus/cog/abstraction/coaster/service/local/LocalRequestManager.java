//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 19, 2008
 */
package org.globus.cog.abstraction.coaster.service.local;

import org.globus.cog.abstraction.coaster.rlog.RemoteLogCommand;
import org.globus.cog.abstraction.coaster.rlog.RemoteLogHandler;
import org.globus.cog.abstraction.coaster.service.ResourceUpdateCommand;
import org.globus.cog.abstraction.coaster.service.ResourceUpdateHandler;
import org.globus.cog.abstraction.coaster.service.job.manager.BQPStatusHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.GetFileHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.PutFileHandler;
import org.globus.cog.coaster.AbstractRequestManager;
import org.globus.cog.coaster.handlers.ChannelConfigurationHandler;
import org.globus.cog.coaster.handlers.HeartBeatHandler;

public class LocalRequestManager extends AbstractRequestManager {
    public static final LocalRequestManager INSTANCE = new LocalRequestManager();
    
    public LocalRequestManager() {
        addHandler(VersionHandler.NAME, VersionHandler.class);
        addHandler(RegistrationHandler.NAME, RegistrationHandler.class);
        addHandler(UnregisterHandler.NAME, UnregisterHandler.class);
        addHandler("CHANNELCONFIG", ChannelConfigurationHandler.class);
        addHandler(JobStatusHandler.NAME, JobStatusHandler.class);
        addHandler(HeartBeatHandler.NAME, HeartBeatHandler.class);
        addHandler(BQPStatusHandler.NAME, BQPStatusHandler.class);
        addHandler(RemoteLogCommand.NAME, RemoteLogHandler.class);
        addHandler("GET", GetFileHandler.class);
        addHandler("PUT", PutFileHandler.class);
        addHandler(ResourceUpdateCommand.NAME, ResourceUpdateHandler.class);
    }
}
