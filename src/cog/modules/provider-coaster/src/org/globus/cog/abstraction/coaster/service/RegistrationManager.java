//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 4, 2009
 */
package org.globus.cog.abstraction.coaster.service;

import java.util.Map;

import org.globus.cog.coaster.channels.CoasterChannel;

public interface RegistrationManager {

    String registrationReceived(String blockID, String workerID,
                                String workerHostname,
                                CoasterChannel channel,
                                Map<String, String> options);

    String nextId(String id);
}
