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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 11, 2010
 */
package org.globus.cog.abstraction.impl.common;

import java.util.EnumSet;

import org.globus.cog.abstraction.interfaces.StagingSetEntry;

public class StagingSetEntryImpl implements StagingSetEntry {
    private final String source, destination;
    private final EnumSet<Mode> mode;
    
    public StagingSetEntryImpl(String source, String destination) {
        this(source, destination, EnumSet.of(Mode.IF_PRESENT));
    }

    public StagingSetEntryImpl(String source, String destination, EnumSet<Mode> mode) {
        if (source == null) {
            throw new NullPointerException("Source cannot be null");
        }
        if (destination == null) {
            throw new NullPointerException("Destination cannot be null");
        }
        this.source = source;
        this.destination = destination;
        this.mode = mode;
    }

    public String getDestination() {
        return destination;
    }

    public String getSource() {
        return source;
    }
    
    public EnumSet<Mode> getMode() {
        return mode;
    }

    public boolean equals(Object obj) {
        if (obj instanceof StagingSetEntry) {
            StagingSetEntry other = (StagingSetEntry) obj;
            return source.equals(other.getSource())
                    && destination.equals(other.getDestination());
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return source.hashCode() + destination.hashCode();
    }

    public String toString() {
        return source + " -> " + destination + "(" + mode + ")";
    }
}
