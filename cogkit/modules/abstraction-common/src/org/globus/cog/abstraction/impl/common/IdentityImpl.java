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
    private long value;
    private static long count = System.currentTimeMillis();
    
    protected static long nextId() {
    	long id;
    	synchronized(IdentityImpl.class) {
    		id = count++;
    	}
    	return id;
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
    
    public IdentityImpl(String namespace, long value) {
        this.nameSpace = namespace;
        this.value = value;
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
        this.value = value;
    }

    public long getValue() {
        return this.value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IdentityImpl other = (IdentityImpl) obj;
        if (nameSpace == null) {
            if (other.nameSpace != null)
                return false;
        }
        else if (!nameSpace.equals(other.nameSpace))
            return false;
        if (value != other.value)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((nameSpace == null) ? 0 : nameSpace.hashCode());
        result = prime * result + (int) (value ^ (value >>> 32));
        return result;
    }

    public String toString() {
        return nameSpace + "-" + value;
    }

    public static Identity parse(String str) {
        int dashpos = str.lastIndexOf('-');
        
        if (dashpos == -1) {
            throw new IllegalArgumentException("Malformed identity: '" + str + "'. Must be of the form <namespace>-<id>[:<subid>*]");
        }
        
        Identity prev = null;
        String ns = str.substring(0, dashpos);
        int lastColPos = dashpos;
        for (int i = dashpos + 1; i < str.length(); i++) {
        	char c = str.charAt(i);
        	if (c == ':') {
    			long value = Long.parseLong(str.substring(lastColPos + 1, i));
    		    if (prev == null) {
    		    	prev = new IdentityImpl(ns, value); 
    		    }
    		    else {
    		    	prev = new CompositeIdentityImpl(prev, value);
    		    }
    		    lastColPos = i;
        	}
        }
        long value = Long.parseLong(str.substring(lastColPos + 1));
        if (prev == null) {
        	prev = new IdentityImpl(ns, value);
        }
        else {
        	prev = new CompositeIdentityImpl(prev, value);
        }
        return prev;
    }
}
