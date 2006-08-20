// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.broker.impl;

import org.globus.cog.broker.interfaces.ServiceManager;

public class DefaultTaskToServiceMapper extends RandomTaskToServiceMapper {

    public DefaultTaskToServiceMapper(ServiceManager serviceManager) {
        super(serviceManager);
    }
}
