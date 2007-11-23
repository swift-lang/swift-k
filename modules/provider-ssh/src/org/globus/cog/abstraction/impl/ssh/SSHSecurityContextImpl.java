//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.ssh;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.interfaces.SecurityContext;

public class SSHSecurityContextImpl implements SecurityContext {

    private static Logger logger = Logger
            .getLogger(SSHSecurityContextImpl.class.getName());
    private Object credentials;

    private Hashtable attributes = new Hashtable();

    public SSHSecurityContextImpl() {
        //  this.credentials = new PasswordAuthentication(null, null);
    }

    public SSHSecurityContextImpl(Object credentials) {
        setCredentials(credentials);
    }

    public void setCredentials(Object credentials, String alias) {
        setCredentials(credentials);
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    public Object getCredentials() throws InvalidSecurityContextException {
        return this.credentials;
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Object getAttribute(String name) {

        return this.attributes.get(name);
    }

    public void setAlias(String alias) {
    }

    public String getAlias() {

        return null;
    }
}
