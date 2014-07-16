//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;

import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.CredentialsDialog;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;

public class InteractivePasswordSecurityContextImpl extends SecurityContextImpl {

    private static Logger logger = Logger
            .getLogger(InteractivePasswordSecurityContextImpl.class.getName());
    private String username, hostName;
    
    public InteractivePasswordSecurityContextImpl(String username) {
        this.username = username;
    }

    public InteractivePasswordSecurityContextImpl() {
    }

    public InteractivePasswordSecurityContextImpl(PasswordAuthentication credentials) {
        setCredentials(credentials);
    }

    public void setCredentials(Object credentials, String alias) {
        setCredentials(credentials);
    }

    public synchronized Object getCredentials() {
        Object credentials = getCredentials();
        if (credentials == null) {
            credentials = CredentialsDialog.showCredentialsDialog(hostName, username);
            setCredentials(credentials);
        }
        return credentials;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
