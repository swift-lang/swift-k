//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.ssh;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;

public class SSHSecurityContextImpl extends SecurityContextImpl {

    private static Logger logger = Logger
            .getLogger(SSHSecurityContextImpl.class.getName());

    public SSHSecurityContextImpl() {
    }

    public SSHSecurityContextImpl(Object credentials) {
        setCredentials(credentials);
    }

    public void setCredentials(Object credentials, String alias) {
        setCredentials(credentials);
    }
}
