//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 20, 2007
 */
package org.globus.cog.abstraction.impl.ssh;

import com.sshtools.j2ssh.session.SessionChannelClient;

public class SSHChannel {
    private Ssh connection;
    private SessionChannelClient session;
    private SSHConnectionBundle bundle;
    
    public SSHChannel(SSHConnectionBundle bundle, Ssh connection, SessionChannelClient session) {
        this.connection = connection;
        this.session = session;
        this.bundle = bundle;
    }

    public Ssh getSsh() {
        return connection;
    }

    public SessionChannelClient getSession() {
        return session;
    }

    public SSHConnectionBundle getBundle() {
        return bundle;
    }
}
