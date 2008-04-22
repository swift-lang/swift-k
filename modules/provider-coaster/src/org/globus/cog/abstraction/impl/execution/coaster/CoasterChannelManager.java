//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import org.globus.cog.abstraction.coaster.service.local.LocalRequestManager;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;

public class CoasterChannelManager extends ChannelManager {
    private static ChannelManager manager;
    
    public synchronized static ChannelManager getManager() {
        if (manager == null) {
            manager = new CoasterChannelManager();
        }
        return manager;
    }
    
    public CoasterChannelManager() {
        super();
        setClientRequestManager(new LocalRequestManager());
    }
}
