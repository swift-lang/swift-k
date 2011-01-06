//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 4, 2009
 */
package org.globus.cog.abstraction.coaster.service;

import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public interface RegistrationManager {
    String registrationReceived(String id, String url, ChannelContext channelContext);

    String nextId(String id);
}
