//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file.webdav;

import java.net.PasswordAuthentication;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.interfaces.SecurityContext;

public class WebDAVSecurityContextImpl implements SecurityContext {

    private static Logger logger = Logger
            .getLogger(WebDAVSecurityContextImpl.class.getName());
    private PasswordAuthentication credentials = null;

    private Hashtable attributes = new Hashtable();

    public WebDAVSecurityContextImpl() {
        //  this.credentials = new PasswordAuthentication(null, null);
    }

    public void setCredentials(Object credentials, String alias) {
        setCredentials(credentials);
    }

    public WebDAVSecurityContextImpl(PasswordAuthentication credentials) {
        setCredentials(credentials);
    }

    public void setCredentials(Object credentials) {
        try {
            this.credentials = (PasswordAuthentication) credentials;
        } catch (Exception e) {
            logger.error("Cannot establish credentials", e);
        }
    }

    public Object getCredentials() throws InvalidSecurityContextException {
        if (credentials == null) {
            throw new InvalidSecurityContextException(
                    "WebDAV provider cannot handle default credentials. Please provide a valid WebDAV credential");
        }
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
