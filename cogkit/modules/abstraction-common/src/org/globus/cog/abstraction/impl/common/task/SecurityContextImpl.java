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

import java.util.HashMap;
import java.util.Map;

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
    
    protected Object getProperty(Map<String, Object> m, String key, Object default_) {
        Object o = m.get(key);
        if (o == null) {
            return default_;
        }
        else {
            return o;
        }
    }
    
    protected Object getProperty(Map<String, Object> m, String key) {
        Object o = m.get(key);
        if (o == null) {
            throw new IllegalArgumentException("Missing credential property '" + key + "'");
        }
        else {
            return o;
        }
    }
    
    protected String getStringProperty(Map<String, Object> m, String key) {
        return toString(getProperty(m, key), key);
    }
    
    protected String getStringProperty(Map<String, Object> m, String key, String default_) {
        return toString(getProperty(m, key, default_), key);
    }

    private String toString(Object o, String key) {
        if (o instanceof String) {
            return (String) o;
        }
        else {
            throw new IllegalArgumentException("Invalid value for credential property '" + key + "'. Must be a string");
        }
    }

    @Override
    public void setCredentialProperties(Map<String, Object> props) {
        throw new UnsupportedOperationException();
    }
}