//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 21, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.channels.ChannelContext;

public interface QueueProcessor {

    public abstract void enqueue(Task t);
    
    public abstract void start();

    public abstract void shutdown();

    public abstract void setClientChannelContext(ChannelContext channelContext);

}