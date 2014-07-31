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

package org.globus.cog.abstraction.impl.common;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.Identity;

/**
 * An implementation of the <code>Identity</code> interface. It
 * calculates a unique value for this <code>Identity</code>by
 * assigning it the value of a static counter that was initialized by
 * the current time of day in milliseconds.
 */
public class IdentityImpl implements Identity {
    static Logger logger = Logger.getLogger(IdentityImpl.class.getName());
    
    private String nameSpace = "cog";
    private String value;
    private static long count = System.currentTimeMillis();
    
    protected static String nextId() {
    	long id;
    	synchronized(IdentityImpl.class) {
    		id = count++;
    	}
    	return String.valueOf(id);
    }

    /**
     * The default constructor. Assigns a default namespace to this
     * identity as <code>cog</code>.
     */
    public IdentityImpl() {
        this.value = nextId();
    }

    /**
    * Instantiates an <code>Identity</code> with the given namespace.
     */
    public IdentityImpl(String namespace) {
        this.value = nextId();
        this.nameSpace = namespace;
    }

    /**
     * Makes a shallow copy of the given
     * <code>Identity</code>. Instantiates an <code>Identity</code> by
     * copying the namespace and value from the given
     * <code>Identity</code>.
     */
    public IdentityImpl(Identity identity) {
        this.nameSpace = identity.getNameSpace();
        this.value = identity.getValue();
    }

    public void setNameSpace(String namespace) {
        this.nameSpace = namespace;
    }

    public String getNameSpace() {
        return this.nameSpace;
    }
    
    public void setValue(long value) {
        setValue(String.valueOf(value));
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Identity id) {
        return compare(value, id.getValue()) && compare(nameSpace, id.getNameSpace());
    }

    private boolean compare(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        else {
            return s1.equals(s2);
        }
    }

    public boolean equals(Object object) {
        return this.toString().equalsIgnoreCase(((Identity) object).toString());
    }

    public int hashCode() {
        int hc = 0;
        hc += nameSpace == null ? 0 : nameSpace.hashCode();
        hc += value == null ? 0 : value.hashCode();
        return hc;
    }

    public String toString() {
        return "urn:" + nameSpace + "-" + value;
    }
}
