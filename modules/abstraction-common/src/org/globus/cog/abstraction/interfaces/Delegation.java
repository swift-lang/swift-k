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
 * Created on Feb 23, 2007
 */
package org.globus.cog.abstraction.interfaces;

/**
 * Encapsulates various delegation settings
 */
public interface Delegation {
    /**
     * Indicates that no delegation should be performed when using this
     * credential
     */
    public static final int NO_DELEGATION = 0;
    /**
     * Indicates that full delegation should be performed when using this
     * credential. If I understand correctly, full delegation allows a
     * theoretically unlimited number of delegations to be further performed
     * from the delegated credential
     */
    public static final int FULL_DELEGATION = 1;
    /**
     * Indicates that partial/limited delegation should be performed when using
     * this credential.
     */
    public static final int PARTIAL_DELEGATION = 2;
    /**
     * Indicates that partial/limited delegation should be performed when using
     * this credential.
     */
    public static final int LIMITED_DELEGATION = PARTIAL_DELEGATION;

}
