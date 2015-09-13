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
public class CompositeIdentityImpl implements Identity {
    static Logger logger = Logger.getLogger(CompositeIdentityImpl.class);
    
    private Identity prev;
    private long value;
    
    public CompositeIdentityImpl(Identity prev, long value) {
        this.prev = prev;
        this.value = value;
    }
    
    public CompositeIdentityImpl(Identity prev) {
        this.prev = prev;
        this.value = IdentityImpl.nextId();
    }

    @Override
    public void setNameSpace(String namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNameSpace() {
        return prev.getNameSpace();
    }

    @Override
    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((prev == null) ? 0 : prev.hashCode());
        result = prime * result + (int) (value ^ (value >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompositeIdentityImpl other = (CompositeIdentityImpl) obj;
        if (prev == null) {
            if (other.prev != null)
                return false;
        }
        else if (!prev.equals(other.prev))
            return false;
        if (value != other.value)
            return false;
        return true;
    }

    public String toString() {
        return prev.toString() + ":" + value;
    }
}
