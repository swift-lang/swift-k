//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 12, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

// import org.globus.cog.abstraction.coaster.service.local.LocalRequestManager;
// import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
// import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.coaster.commands.Command;

public class WorkerShellCommand extends Command {
    public static final String NAME = "WORKERSHELLCMD";
    
    public WorkerShellCommand(String workerId, String command) {
        super(NAME);
        addOutData(workerId);
        addOutData(command);
    }

    public void input(String str) {
        // TBD
    }
}
