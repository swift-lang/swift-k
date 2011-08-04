//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file.webdav;

import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;

public class InteractiveWebDAVSecurityContextImpl extends SecurityContextImpl {

    private static Logger logger = Logger
            .getLogger(InteractiveWebDAVSecurityContextImpl.class.getName());

    public InteractiveWebDAVSecurityContextImpl() {
    }

    public InteractiveWebDAVSecurityContextImpl(PasswordAuthentication credentials) {
        setCredentials(credentials);
    }

    public synchronized Object getCredentials() {
        Object credentials = getCredentials();
        if (credentials == null) {
            credentials = CredentialsDialog.showCredentialsDialog();
            setCredentials(credentials);
        }
        return credentials;
    }
}
