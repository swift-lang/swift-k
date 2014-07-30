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
 * Created on Jun 9, 2011
 */
package org.globus.cog.abstraction.interfaces;

/**
 * 
 * Represents a fragment of a file (or an entire file).
 */
public interface FileFragment {
    public static final long MAX_LENGTH = Long.MAX_VALUE;
    public static final long FILE_START = 0;

    /**
     * Returns the full file name (including path). The path
     * can be relative or absolute and uses the UNIX convention
     * (i.e. "/" is the path separator).
     */
    String getFile();

    /**
     * Returns the offset in the file of the fragment.
     */
    long getOffset();
    
    /**
     * Returns the length of the fragment or {@link #MAX_LENGTH} 
     * for up to the end of the file.
     */
    long getLength();
    
    /**
     * Returns <tt>true</tt> if this fragment does not represent an
     * entire file
     */
    boolean isFragment();
}
