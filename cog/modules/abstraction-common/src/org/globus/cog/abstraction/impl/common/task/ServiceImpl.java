// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public class ServiceImpl implements Service {
    private Identity identity = null;
    private String name = "";
    private ServiceContact serviceContact;
    private SecurityContext securityContext = null;
    private Map attributes;
    private String provider = null;
    private int type = 0;
    private int totalCount = 0, failedCount = 0, activeCount = 0;

    public ServiceImpl() {
    }

    public ServiceImpl(int type) {
        this.type = type;
    }

    public ServiceImpl(String provider, ServiceContact serviceContact,
            SecurityContext securityContext) {
        this();
        this.provider = provider;
        this.serviceContact = serviceContact;
        this.securityContext = securityContext;
    }

    public ServiceImpl(String provider, int type,
            ServiceContact serviceContact, SecurityContext securityContext) {
        this(type);
        this.provider = provider;
        this.serviceContact = serviceContact;
        this.securityContext = securityContext;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Identity getIdentity() {
        return this.identity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return this.provider;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void setServiceContact(ServiceContact serviceContact) {
        this.serviceContact = serviceContact;
    }

    public ServiceContact getServiceContact() {
        return serviceContact;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public SecurityContext getSecurityContext() {
        return this.securityContext;
    }

    public void setCompletedCount(int count) {
        this.totalCount = count;
    }

    public int getCompletedCount() {
        return this.totalCount;
    }

    public void setActiveCount(int count) {
        this.activeCount = count;
    }

    public int getActiveCount() {
        return this.activeCount;
    }

    public void setFailedCount(int count) {
        this.failedCount = count;
    }

    public int getFailedCount() {
        return this.failedCount;
    }

    public void setAttribute(String name, Object value) {
        if (attributes == null) {
            attributes = new HashMap();
        }
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        if (attributes != null) {
            return attributes.get(name);
        }
        else {
            return null;
        }
    }

    public Enumeration getAllAttributes() {
        return new Vector(getAttributeNames()).elements();
    }

    public Collection getAttributeNames() {
        if (attributes != null) {
            return attributes.keySet();
        }
        else {
            return Collections.EMPTY_MAP.keySet();
        }
    }

    public String toString() {
        return serviceContact.toString() + "(" + this.provider + ")";
    }

    public int hashCode() {
        return serviceContact.hashCode() + provider.hashCode()
                + (securityContext == null ? 0 : securityContext.hashCode());
    }

    public boolean equals(Object o) {
        if (o instanceof Service) {
            Service s = (Service) o;
            return serviceContact.equals(s.getServiceContact())
                    && provider.equals(s.getProvider())
                    && equals(securityContext, s.getSecurityContext());
        }
        return false;
    }

    private boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
}
