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

import java.io.File;
import java.util.Calendar;
import java.util.Enumeration;

import org.globus.cog.abstraction.xml.MarshalException;

/**
 * A <code>TaskGraph</code> provides a building block for expressing complex
 * dependencies between tasks. All advanced applications require mechanisms to
 * execute client-side workflows that process the tasks based on user-defined
 * control {@link Dependency}. Hence, the <code>TaskGraph</code> aggregates a
 * set of {@link ExecutableObject}s (<code>Task</code> or
 * <code>TaskGraph</code>) and allows the user to define dependencies between
 * these tasks. In graph theoretical terms, a <code>TaskGraph</code> provides
 * the mechanism to express workflows as a hierarchical directed acyclic graph.
 * A <code>TaskGraph</code> can theoretically contain infinite levels of
 * hierarchy. However, practically it is constrained with the availability of
 * resources (memory) on a particular system.
 * 
 * @deprecated Not being maintained
 */
public interface TaskGraph extends ExecutableObject {
    /**
     * Represents a failure policy whereby the execution of the
     * <code>TaskGraph</code> is aborted once any of its component
     * <code>Task</code> fails.
     */
    public static int AbortOnFailure = 1;

    /**
     * Represents a failure policy whereby the execution of the
     * <code>TaskGraph</code> is continued even if its component
     * <code>Task</code> fail.
     */
    public static int ContinueOnFailure = 2;

    /**
     * Adds the {@link ExecutableObject}(<code>Task</code> or
     * <code>TaskGraph</code>) to this <code>TaskGraph</code>. By default
     * the added node is an unconnected node without dependencies on any of the
     * existing nodes in this <code>TaskGraph</code>.
     * 
     * @param graphNode
     *            an {@link ExecutableObject}to be added as a node
     */
    public void add(ExecutableObject graphNode) throws Exception;

    /**
     * Removes the {@link ExecutableObject}that is represented by the given
     * identity.
     * 
     * @param id
     *            the identity of the node to be removed
     * @return the removed {@link ExecutableObject}
     */
    public ExecutableObject remove(Identity id) throws Exception;

    /**
     * Returns the {@link ExecutableObject}that is represented by the given
     * identity.
     * 
     * @param id
     *            the identity of the node to be returned
     * @return the {@link ExecutableObject}represented by the given Identity
     */
    public ExecutableObject get(Identity id);

    /**
     * Returns all the {@link ExecutableObject}(s) that is encapsulated by this
     * <code>TaskGraph</code>.
     * 
     * @return the array containing all the nodes of this <code>TaskGraph</code>
     */
    public ExecutableObject[] toArray();

    /**
     * Returns all the {@link ExecutableObject}(s) that is encapsulated by this
     * <code>TaskGraph</code>.
     * 
     * @return the enumeration containing all the nodes of this
     *         <code>TaskGraph</code>
     */
    public Enumeration elements();

    /**
     * Sets all the dependencies associated with this <code>TaskGraph</code>.
     */
    public void setDependency(Dependency dependency);

    /**
     * Returns the dependency object associated with this <code>TaskGraph</code>.
     */
    public Dependency getDependency();

    /**
     * Adds a single dependency between the given nodes.
     * 
     * @param from
     *            the {@link ExecutableObject}representing the parent node
     * @param to
     *            the {@link ExecutableObject}representing the child node
     */
    public void addDependency(ExecutableObject from, ExecutableObject to);

    /**
     * Removes the dependency between the given nodes.
     * 
     * @param from
     *            the {@link ExecutableObject}representing the parent node
     * @param to
     *            the {@link ExecutableObject}representing the child node
     */
    public boolean removeDependency(ExecutableObject from, ExecutableObject to);

    public void setAttribute(String name, Object value);

    public Object getAttribute(String name);

    public Enumeration getAllAttributes();

    /**
     * Returns the total number of {@link ExecutableObject}encapsulated by this
     * <code>TaskGraph</code>
     */
    public int getSize();

    /**
     * Returns a boolean indicating whether this <code>TaskGraph</code>
     * contains any node
     */
    public boolean isEmpty();

    /**
     * Returns a boolean indicating whether this <code>TaskGraph</code>
     * contains a node with the given Identity
     * 
     * @param id
     *            the Identity of the desired node
     */
    public boolean contains(Identity id);

    /**
     * Returns the number of {@link ExecutableObject}s that have their status
     * as {@link Status#UNSUBMITTED}
     */
    public int getUnsubmittedCount();

    /**
     * Returns the number of {@link ExecutableObject}s that have their status
     * as {@link Status#SUBMITTED}
     */
    public int getSubmittedCount();

    /**
     * Returns the number of {@link ExecutableObject}s that have their status
     * as {@link Status#ACTIVE}
     */
    public int getActiveCount();

    /**
     * Returns the number of {@link ExecutableObject}s that have their status
     * as {@link Status#COMPLETED}
     */
    public int getCompletedCount();

    /**
     * Returns the number of {@link ExecutableObject}s that have their status
     * as {@link Status#SUSPENDED}
     */
    public int getSuspendedCount();

    /**
     * Returns the number of {@link ExecutableObject}s that have their status
     * as {@link Status#RESUMED}
     */
    public int getResumedCount();

    /**
     * Returns the number of {@link ExecutableObject}s that have their status
     * as {@link Status#FAILED}
     */
    public int getFailedCount();

    /**
     * Returns the number of {@link ExecutableObject}s that have their status
     * as {@link Status#CANCELED}
     */
    public int getCanceledCount();

    /**
     * Checkpoints the current state of this <code>TaskGraph</code> in XML
     * format. Using the {@link org.globus.cog.abstraction.xml.TaskGraphUnmarshaller},
     * the checkpointed <code>TaskGraph</code> can once again be
     * re-instantiated.
     * 
     * @param file
     *            the file to store the checkpointed task graph.
     * @throws MarshalException
     *             during an error in XML translation.
     */
    public void toXML(File file) throws MarshalException;

    /**
     * Returns the time when this <code>TaskGraph</code> was submitted
     * 
     * @return the <code>TaskGraph</code> submission time. null, if it is not
     *         yet submitted
     */
    public Calendar getSubmittedTime();

    /**
     * Returns the time when this <code>TaskGraph</code> was completed
     * 
     * @return the <code>TaskGraph</code> completion time. null, if it is not
     *         yet completed
     */
    public Calendar getCompletedTime();

    /**
     * Sets the policy adopted by this <code>TaskGraph</code> when any
     * <code>Task</code> fails. This policy determines the execution behaviour
     * of the nodes dependent on the failed nodes.
     * <p>
     * For example, if the policy is {@link TaskGraph#AbortOnFailure}, then
     * dependents of a failed <code>ExecutableObject</code> are not executed
     * at all. On the other hand, if the policy is
     * {@link TaskGraph#ContinueOnFailure}, then the dependents are executed
     * irrespective to the final status of the parents.
     */
    public void setFailureHandlingPolicy(int policy);

    /**
     * Returns the failure handling policy of this <code>TaskGraph</code>.
     */
    public int getFailureHandlingPolicy();

    /**
     * Adds the listener to receive graph change events produced by this
     * <code>TaskGraph</code>. Change events are generated when an
     * <code>ExecutableObject</code> is either added or removed from this
     * <code>TaskGraph</code>.
     */
    public void addChangeListener(ChangeListener listener);

    /**
     * Removes the change listener for this <code>TaskGraph</code>.
     */
    public void removeChangeListener(ChangeListener listener);
}