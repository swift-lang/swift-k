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
 * Created on Aug 20, 2007
 */
package org.globus.cog.abstraction.interfaces;

import java.io.Serializable;

public interface FileLocation extends Serializable {

    /**
     * Specifies that nothing should be done with a job output stream or that
     * there is nothing provided on the input stream.
     */
    public static final FileLocation NONE = new Impl(0);

    /**
     * Indicates that the contents of the stream is in memory. The contents can
     * the be retrieved using one of the <code>Task.getXYZContents</code>
     * methods.
     */
    public static final FileLocation MEMORY = new Impl(1);
    /**
     * Indicates that the contents of a stream is or is to be redirected to a
     * local file.
     */
    public static final FileLocation LOCAL = new Impl(2);

    /**
     * Indicates that the contents of a stream is or is to be redirected to a
     * remote file.
     */
    public static final FileLocation REMOTE = new Impl(4);

    public static final FileLocation MEMORY_AND_LOCAL = MEMORY.and(LOCAL);

    /**
     * Can be used to combine to stream types. The resulting object indicates,
     * for output streams, that both (or all) redirections should be used.
     */
    FileLocation and(FileLocation other);
    
    /**
     * Returns a location that contains all the locations in this object
     * that are not contained in the parameter.
     */
    FileLocation remove(FileLocation other);

    /**
     * Returns <code>true</code> if this StreamType is the same as the
     * specified StreamType or it can be derived from the specified StreamType
     * using a certain number of <code>and</code> operations.
     */
    boolean includes(FileLocation other);

    /**
     * Returns <code>true</code> if there is any overlap between this location
     * and the specified location. For example,
     * <code>FileLocation.MEMORY.matches(FileLocation.LOCAL.and(FileLocation.MEMORY))</code>
     * would return <code>true</code>, while
     * <code>FileLocation.LOCAL.and(FileLocation.REMOTE).matches(FileLocation.MEMORY)</code>
     * would return <code>false</code>.
     */
    boolean overlaps(FileLocation other);

    int getCode();

    public static class Impl implements FileLocation, Serializable {
        private int code;

        public Impl(int code) {
            this.code = code;
        }

        public FileLocation and(FileLocation other) {
            return new Impl(code | other.getCode());
        }
        
        public FileLocation remove(FileLocation other) {
            return new Impl(code & (~other.getCode()));
        }

        public boolean includes(FileLocation other) {
            return (code & other.getCode()) == code;
        }

        public boolean overlaps(FileLocation other) {
            return (code & other.getCode()) != 0;
        }

        public int getCode() {
            return code;
        }

        public boolean equals(Object other) {
            if (other instanceof FileLocation) {
                return code == ((FileLocation) other).getCode();
            }
            else {
                return false;
            }
        }

        public int hashCode() {
            return code;
        }

        private static final String[] LOCS = new String[] { "memory", "local",
                "remote" };

        public String toString() {
            if (code == 0) {
                return "none";
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 3; i++) {
                if ((code & (1 << i)) != 0) {
                    append(sb, LOCS[i]);
                }
            }
            return sb.toString();
        }
        
        private void append(StringBuffer sb, String m) {
            if (sb.length() != 0) {
                sb.append(" + ");
            }
            sb.append(m);
        }
    }
}
