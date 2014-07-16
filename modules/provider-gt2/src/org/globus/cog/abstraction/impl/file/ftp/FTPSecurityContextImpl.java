//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file.ftp;

import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;

public class FTPSecurityContextImpl extends SecurityContextImpl {

    private static Logger logger = Logger
            .getLogger(FTPSecurityContextImpl.class.getName());

    public FTPSecurityContextImpl() {
    }

    public FTPSecurityContextImpl(PasswordAuthentication credentials) {
        setCredentials(credentials);
    }

    public void setAlias(String alias) {
    }

    public String getAlias() {
        return null;
    }
}
