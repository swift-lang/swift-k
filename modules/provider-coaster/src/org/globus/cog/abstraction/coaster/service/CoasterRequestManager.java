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
import org.globus.cog.abstraction.impl.file.coaster.commands.ChmodCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.DeleteCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.ExistsCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.FileInfoCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.GetFileCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.IsDirectoryCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.ListCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.MkdirCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.PutFileCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.RenameCommand;
import org.globus.cog.abstraction.impl.file.coaster.commands.RmdirCommand;
import org.globus.cog.abstraction.impl.file.coaster.handlers.ChmodHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.DeleteHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.ExistsHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.FileInfoHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.GetFileHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.IsDirectoryHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.ListHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.MkdirHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.PutFileHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.RenameHandler;
import org.globus.cog.abstraction.impl.file.coaster.handlers.RmdirHandler;
import org.globus.cog.coaster.AbstractRequestManager;
import org.globus.cog.coaster.commands.InfoCommand;
import org.globus.cog.coaster.handlers.ChannelConfigurationHandler;
import org.globus.cog.coaster.handlers.HeartBeatHandler;
import org.globus.cog.coaster.handlers.InfoHandler;
import org.globus.cog.coaster.handlers.ShutdownHandler;
import org.globus.cog.coaster.handlers.VersionHandler;

public class CoasterRequestManager extends AbstractRequestManager {   
    public CoasterRequestManager() {
        addHandler("VERSION", VersionHandler.class);
        addHandler("CHANNELCONFIG", ChannelConfigurationHandler.class);
        addHandler("SHUTDOWN", ShutdownHandler.class);
        addHandler("HEARTBEAT", HeartBeatHandler.class);
        addHandler(InfoCommand.NAME, InfoHandler.class);
        addHandler(SubmitJobCommand.NAME, SubmitJobHandler.class);
        addHandler(ServiceShutdownHandler.NAME, ServiceShutdownHandler.class);
        addHandler(WorkerShellHandler.NAME, WorkerShellHandler.class);
        addHandler(ServiceConfigurationHandler.NAME, ServiceConfigurationHandler.class);
        
        addHandler(ExistsCommand.NAME, ExistsHandler.class);
        addHandler(DeleteCommand.NAME, DeleteHandler.class);
        addHandler(MkdirCommand.NAME, MkdirHandler.class);
        addHandler(ListCommand.NAME, ListHandler.class);
        addHandler(RmdirCommand.NAME, RmdirHandler.class);
        addHandler(FileInfoCommand.NAME, FileInfoHandler.class);
        addHandler(GetFileCommand.NAME, GetFileHandler.class);
        addHandler(PutFileCommand.NAME, PutFileHandler.class);
        addHandler(ChmodCommand.NAME, ChmodHandler.class);
        addHandler(RenameCommand.NAME, RenameHandler.class);
        addHandler(IsDirectoryCommand.NAME, IsDirectoryHandler.class);
    }
}
