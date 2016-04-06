//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 23, 2016
 */
package org.globus.cog.abstraction.impl.common.task;

import java.util.Map;

public class DefaultSecurityContext extends SecurityContextImpl {

    @Override
    public void setCredentialProperties(Map<String, Object> props) {
        throw new UnsupportedOperationException();
    }
}
