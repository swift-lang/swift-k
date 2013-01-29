//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 12, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import org.globus.cog.coaster.commands.Command;

public class CancelJobCommand extends Command {
    public static final String NAME = "CANCELJOB";
    
    public CancelJobCommand(String jobid) {
        super(NAME);
        addOutData(jobid);
    }    
}
