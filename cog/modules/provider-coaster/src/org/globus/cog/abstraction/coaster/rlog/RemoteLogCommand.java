//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 14, 2009
 */
package org.globus.cog.abstraction.coaster.rlog;

import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.commands.Command;

public class RemoteLogCommand extends Command {
    public static final String NAME = "RLOG";
    
    private final String msg;
    
    public RemoteLogCommand(String msg) {
        super(NAME);
        this.msg = msg;
    }

    public void send() throws ProtocolException {
        addOutData(msg);
        super.send();
    }
}
