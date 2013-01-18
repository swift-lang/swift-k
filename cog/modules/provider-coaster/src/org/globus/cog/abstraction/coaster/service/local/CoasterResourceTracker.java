//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 9, 2011
 */
package org.globus.cog.abstraction.coaster.service.local;

import org.globus.cog.abstraction.interfaces.Service;

public interface CoasterResourceTracker {
    void resourceUpdated(Service service, String name, String value);
}
