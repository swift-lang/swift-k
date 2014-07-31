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

package org.globus.cog.abstraction.interfaces;

/**
 * An <code>ExecutableObject</code> provides a high-level abstraction for
 * elements that can be executed on the Grid. It can be specialized as a Grid
 * {@link Task}or a {@link TaskGraph}. Every <code>ExecutableObject</code>
 * in the abstractions framework has a unique {@link Identity}and an execution
 * {@link Status}.
 */
public interface ExecutableObject {

    /**
     * Represents a {@link Task}type <code>ExecutableObject</code>
     */
    public static final int TASK = 1;

    /**
     * Represents a {@link TaskGraph}type <code>ExecutableObject</code>
     */
    public static final int TASKGRAPH = 2;

    /**
     * Sets the name of this <code>ExecutableObject</code>. Defines a
     * user-friendly name which need not be unique.
     * 
     * @param name
     *            a string specifying the name of this
     *            <code>ExecutableObject</code>.
     */
    public void setName(String name);

    /**
     * Returns the user-friendly name assigned to this
     * <code>ExecutableObject</code>.
     */
    public String getName();

    /**
     * Sets a unique <code>Identity</code> for this
     * <code>ExecutableObject</code>.
     * 
     * @param id
     *            the unique <code>Identity</code>.
     */
    public void setIdentity(Identity id);

    /**
     * Returns the unique <code>Identity</code> assigned to this
     * <code>ExecutableObject</code>.
     */
    public Identity getIdentity();

    /**
     * Returns the type of this <code>ExecutableObject</code>. Currently, two
     * types are supported: {@link Task}and {@link TaskGraph}. Additional
     * types can be supported by classes implementing this interface.
     */
    public int getObjectType();

    /**
     * Sets the current <code>Status</code> of this
     * <code>ExecutableObject</code>.
     * 
     * @param status
     *            the latest status of this <code>ExecutableObject</code>.
     */
    public void setStatus(Status status);

    /**
     * Sets the current status of this <code>ExecutableObject</code>.
     * Supported status are:
     * {@link Status#UNSUBMITTED}, {@link Status#SUBMITTING}, {@link Status#SUBMITTED}, {@link Status#ACTIVE},
     * {@link Status#SUSPENDED},{@link Status#RESUMED},{@link Status#FAILED},
     * {@link Status#CANCELED},{@link Status#COMPLETED},
     * {@link Status#UNKNOWN}
     *  
     */
    public void setStatus(int status);

    /**
     * Returns the current <code>Status</code> of this
     * <code>ExecutableObject</code>.
     */
    public Status getStatus();

    /**
     * Adds a listener to receive status events when the status of an
     * <code>ExecutableObject</code> is changed.
     * 
     * @param listener
     *            the status listener
     */
    public void addStatusListener(StatusListener listener);

    /**
     * Removes the status listener from the list of active listeners.
     */
    public void removeStatusListener(StatusListener listener);

}