/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    public static final ServiceContact DEFAULT_CONTACT = new ServiceContactImpl("localhost");
    
    private Identity identity;
    private String name = "";
    private ServiceContact serviceContact;
    private SecurityContext securityContext;
    private Map<String, Object> attributes;
    private String provider = null;
    private int type = 0;
    private int totalCount = 0, failedCount = 0, activeCount = 0;

    public ServiceImpl() {
        serviceContact = DEFAULT_CONTACT;
    }

    public ServiceImpl(int type) {
        this.type = type;
        serviceContact = DEFAULT_CONTACT;
    }

    public ServiceImpl(String provider, ServiceContact serviceContact,
            SecurityContext securityContext) {
        this();
        this.provider = provider;
        if (serviceContact == null) {
            this.serviceContact = DEFAULT_CONTACT;
        }
        else {
            this.serviceContact = serviceContact;
        }
        this.securityContext = securityContext;
    }

    public ServiceImpl(String provider, int type,
            ServiceContact serviceContact, SecurityContext securityContext) {
        this(type);
        this.provider = provider;
        if (serviceContact == null) {
            throw new NullPointerException("serviceContact");
        }
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
        if (serviceContact == null) {
            throw new NullPointerException("serviceContact");
        }
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
            attributes = new HashMap<String, Object>();
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

    @SuppressWarnings("unchecked")
    /** 
       @deprecated
       @see org.globus.cog.abstraction.interfaces.Service#getAllAttributes()
     */
    public Enumeration getAllAttributes() {
        return new Vector(getAttributeNames()).elements();
    }

    public Collection<String> getAttributeNames() {
        if (attributes != null) {
            return attributes.keySet();
        }
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (attributes != null) {
            attributes.remove(name);
        }
    }

    public String toString() {
        return serviceContact + "(" + this.provider + ")";
    }

    public int hashCode() {
        return serviceContact.hashCode() + provider.hashCode()
                + (securityContext == null ? 0 : securityContext.hashCode());
    }

    public boolean equals(Object o) {
        if (o instanceof Service) {
            Service s = (Service) o;
            return equals(identity, s.getIdentity())
                    && serviceContact.equals(s.getServiceContact())
                    && provider.equals(s.getProvider())
                    && equals(securityContext, s.getSecurityContext());
        }
        return false;
    }

    private boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }    
}
