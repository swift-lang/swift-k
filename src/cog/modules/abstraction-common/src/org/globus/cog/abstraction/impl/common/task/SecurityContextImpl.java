// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.HashMap;

import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public class SecurityContextImpl implements SecurityContext {
    private HashMap<String, Object> attributes;
    private Object credentials;
    private String alias;
    private ServiceContact serviceContact;

    public SecurityContextImpl() {
        this.attributes = new HashMap<String, Object>();
    }
    
    public SecurityContextImpl(Object credentials) {
        this();
        this.credentials = credentials;
    }

    public SecurityContextImpl(Object credentials, String alias) {
        this();
        this.credentials = credentials;
        this.alias = alias;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    public Object getCredentials() {
        return this.credentials;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setCredentials(Object credentials, String alias) {
        this.credentials = credentials;
        this.alias = alias;
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }
    
    public int hashCode() {
        return (credentials == null ? 0 : credentials.hashCode()) + attributes.hashCode();
    }
    
    public boolean equals(Object o) {
        if (o instanceof SecurityContext) {
            SecurityContext sc = (SecurityContext) o;
            if ((credentials == null && sc.getCredentials() == null) || credentials.equals(sc.getCredentials())) {
                if (o instanceof SecurityContextImpl) {
                    SecurityContextImpl sci = (SecurityContextImpl) o;
                    return attributes.equals(sci.attributes);
                }
                else {
                    return true;
                }
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public Object getDefaultCredentials() {
        return null;
    }

    public ServiceContact getServiceContact() {
        return serviceContact;
    }

    public void setServiceContact(ServiceContact serviceContact) {
        this.serviceContact = serviceContact;
    }
}